package com.project.kineai.service;

import com.project.kineai.dto.request.CreatePlanRequest;
import com.project.kineai.dto.response.RehabPlanResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.mapper.RehabPlanMapper;
import com.project.kineai.model.entity.Exercise;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.*;
import com.project.kineai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RehabPlanService {

    private final RehabPlanRepository planRepository;
    private final PatientRepository patientRepository;
    private final SessionRepository sessionRepository;
    private final ExerciseRepository exerciseRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final RehabPlanMapper planMapper;

    // Jours d'entraînement par semaine — 3x/semaine
    private static final SessionDay[] TRAINING_DAYS = {
            SessionDay.LUNDI,
            SessionDay.MERCREDI,
            SessionDay.VENDREDI
    };

    @Transactional
    public RehabPlanResponse generatePlan(
            CreatePlanRequest request) {

        Patient patient = patientRepository
                .findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException(
                        "Patient introuvable"));

        planRepository.findByPatientIdAndStatus(
                        patient.getId(), Status.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(Status.DONE);
                    planRepository.save(existing);
                });

        Level level = determineLevel(patient);

        LocalDate start = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        RehabPlan plan = RehabPlan.builder()
                .patient(patient)
                .startDate(start)
                .endDate(start.plusDays(28))
                .status(Status.ACTIVE)
                .difficultyLevel(level)
                .currentWeek(1)
                .build();

        plan = planRepository.save(plan);

        // ✅ Si ça échoue ici, rollback complet —
        // y compris le plan ET l'archivage de
        // l'ancien plan actif (annulé aussi)
        assignExercisesToplan(plan, patient, level);

        log.info("Plan généré pour patient {} — niveau {}",
                patient.getId(), level);
        return planMapper.toResponse(plan);
    }

    // ── Système Expert — assignation automatique ──
// des exercices selon pathologie + niveau du plan
    // ── Système Expert — assignation automatique ──
// des exercices selon pathologie + niveau du plan
// Alterne entre les exercices disponibles (1 par
// jour, pas tous empilés) et augmente légèrement
// les répétitions au fil des semaines
    private void assignExercisesToplan(
            RehabPlan plan, Patient patient, Level level) {

        BodyZone zone = BodyZone.valueOf(
                patient.getPathology().name());

        List<Exercise> matchingExercises =
                exerciseRepository
                        .findByBodyZoneAndDifficultyLevel(
                                zone, level);

        if (matchingExercises.isEmpty()) {
            throw new BusinessException(
                    "Aucun exercice disponible pour la zone "
                            + zone + " au niveau " + level
                            + ". Contactez l'administrateur pour"
                            + " ajouter des exercices correspondants.");
        }

        int exerciseCount = matchingExercises.size();
        int dayIndex = 0; // compteur global tous jours confondus

        for (int week = 1; week <= 4; week++) {
            for (SessionDay day : TRAINING_DAYS) {

                // ✅ Alternance — un seul exercice par jour,
                // en tournant sur la liste disponible
                Exercise exercise = matchingExercises.get(
                        dayIndex % exerciseCount);
                dayIndex++;

                // ✅ Progression légère des répétitions
                // selon la semaine — +2 reps par semaine
                // par rapport à la base de l'exercice
                int baseReps = exercise.getRepsTarget() != null
                        ? exercise.getRepsTarget() : 10;
                int progressiveReps = baseReps + (week - 1) * 2;

                PlanExercise pe = PlanExercise.builder()
                        .rehabPlan(plan)
                        .exercise(exercise)
                        .weekNumber(week)
                        .dayOfWeek(day)
                        .repsPrescribed(progressiveReps)
                        .orderInSession(1)
                        .build();
                planExerciseRepository.save(pe);
            }
        }

        log.info("{} exercice(s) disponibles, assignés en"
                        + " alternance sur 12 séances (4 semaines"
                        + " × 3 jours) pour le plan {}",
                exerciseCount, plan.getId());
    }

    // ── Plan actif du patient connecté ────────
    @Transactional(readOnly = true)
    public RehabPlanResponse getActivePlanForCurrentPatient() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Patient patient = patientRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException(
                        "Patient introuvable"));

        return planMapper.toResponse(
                planRepository.findByPatientIdAndStatus(
                                patient.getId(), Status.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aucun plan actif")));
    }

    // ── Règle RG-19/RG-20 ────────────────────
    @Transactional
    public void checkProgression(UUID patientId) {

        long totalCompleted = sessionRepository
                .countCompletedSessions(patientId);

        if (totalCompleted < 3) {
            log.info("Patient {} — seulement {} séance(s)"
                            + " complétée(s), progression non"
                            + " évaluée (minimum 3 requis)",
                    patientId, totalCompleted);
            return;
        }

        Double avgScore = sessionRepository
                .getAverageScoreLastThreeSessions(patientId);
        if (avgScore == null) return;

        Patient patient = patientRepository
                .findById(patientId)
                .orElseThrow(() -> new RuntimeException(
                        "Patient introuvable"));

        planRepository.findByPatientIdAndStatus(
                        patientId, Status.ACTIVE)
                .ifPresent(plan -> {
                    if (avgScore > 85.0
                            && plan.getDifficultyLevel()
                            != Level.AVANCE) {
                        Level newLevel = upgradeLevel(
                                plan.getDifficultyLevel());
                        plan.setDifficultyLevel(newLevel);
                        patient.setLevel(newLevel);
                        planRepository.save(plan);
                        patientRepository.save(patient);
                        log.info("Progression patient {} → {}",
                                patientId, newLevel);
                    } else if (avgScore < 50.0
                            && plan.getDifficultyLevel()
                            != Level.DEBUTANT) {
                        Level newLevel = downgradeLevel(
                                plan.getDifficultyLevel());
                        plan.setDifficultyLevel(newLevel);
                        patient.setLevel(newLevel);
                        planRepository.save(plan);
                        patientRepository.save(patient);
                        log.info("Régression patient {} → {}",
                                patientId, newLevel);
                    }
                });
    }

    private Level determineLevel(Patient patient) {
        if (patient.getAge() >= 65) return Level.DEBUTANT;
        return patient.getLevel();
    }

    private Level upgradeLevel(Level current) {
        return switch (current) {
            case DEBUTANT      -> Level.INTERMEDIAIRE;
            case INTERMEDIAIRE -> Level.AVANCE;
            case AVANCE        -> Level.AVANCE;
        };
    }

    private Level downgradeLevel(Level current) {
        return switch (current) {
            case AVANCE        -> Level.INTERMEDIAIRE;
            case INTERMEDIAIRE -> Level.DEBUTANT;
            case DEBUTANT      -> Level.DEBUTANT;
        };
    }

    public RehabPlanResponse getActivePlan(UUID patientId) {
        return planMapper.toResponse(
                planRepository.findByPatientIdAndStatus(
                                patientId, Status.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aucun plan actif")));
    }

    public List<RehabPlanResponse> getAllPlans(
            UUID patientId) {
        return planMapper.toResponseList(
                planRepository
                        .findByPatientIdOrderByStartDateDesc(
                                patientId));
    }

    // ── Avancement automatique de semaine ─────
// Appelée après chaque séance complétée —
// si les 3 séances de la semaine courante sont
// faites, on passe à la semaine suivante
    @Transactional
    public void checkWeekAdvancement(UUID planId) {
        RehabPlan plan = planRepository.findById(planId)
                .orElse(null);
        if (plan == null
                || plan.getStatus() != Status.ACTIVE) {
            return;
        }

        int currentWeek = plan.getCurrentWeek();

        LocalDate weekStart = plan.getStartDate()
                .plusDays((currentWeek - 1) * 7L);
        LocalDate weekEnd = weekStart.plusDays(7);

        long sessionsThisWeek = sessionRepository
                .countByRehabPlanIdAndSessionStatusAndStartTimeBetween(
                        plan.getId(),
                        SessionStatus.COMPLETED,
                        weekStart.atStartOfDay(),
                        weekEnd.atStartOfDay());

        // 3 séances/semaine — seuil cohérent avec
        // TRAINING_DAYS dans ExerciseService
        if (sessionsThisWeek >= 3) {
            if (currentWeek >= 4) {
                // Plan de 4 semaines terminé
                plan.setStatus(Status.DONE);
                planRepository.save(plan);
                log.info("Plan {} terminé après 4 semaines",
                        plan.getId());
            } else {
                plan.setCurrentWeek(currentWeek + 1);
                planRepository.save(plan);
                log.info("Plan {} — passage à la semaine {}",
                        plan.getId(), currentWeek + 1);
            }
        }
    }

}