package com.project.kineai.service;

import com.project.kineai.dto.request.CreatePlanRequest;
import com.project.kineai.dto.response.RehabPlanResponse;
import com.project.kineai.exception.BusinessException;
import com.project.kineai.mapper.RehabPlanMapper;
import com.project.kineai.model.entity.Exercise;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.SessionDay;
import com.project.kineai.model.enums.Status;
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
    private void assignExercisesToplan(
            RehabPlan plan, Patient patient, Level level) {

        BodyZone zone = BodyZone.valueOf(
                patient.getPathology().name());

        List<Exercise> matchingExercises =
                exerciseRepository
                        .findByBodyZoneAndDifficultyLevel(
                                zone, level);

        // ✅ Message clair pour le kiné, pas juste un log
        if (matchingExercises.isEmpty()) {
            throw new BusinessException(
                    "Aucun exercice disponible pour la zone "
                            + zone + " au niveau " + level
                            + ". Contactez l'administrateur pour"
                            + " ajouter des exercices correspondants.");
        }

        for (int week = 1; week <= 4; week++) {
            for (SessionDay day : TRAINING_DAYS) {
                int order = 1;
                for (Exercise exercise : matchingExercises) {
                    PlanExercise pe = PlanExercise.builder()
                            .rehabPlan(plan)
                            .exercise(exercise)
                            .weekNumber(week)
                            .dayOfWeek(day)
                            .repsPrescribed(
                                    exercise.getRecommendedDuration() != null
                                            ? exercise.getRecommendedDuration()
                                            : 10)
                            .orderInSession(order++)
                            .build();
                    planExerciseRepository.save(pe);
                }
            }
        }

        log.info("{} exercice(s) assignés automatiquement"
                        + " sur 12 séances pour le plan {}",
                matchingExercises.size(), plan.getId());
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

}