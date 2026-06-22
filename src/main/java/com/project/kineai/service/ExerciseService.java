package com.project.kineai.service;

import com.project.kineai.dto.response.ExerciseResponse;
import com.project.kineai.mapper.ExerciseMapper;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.SessionDay;
import com.project.kineai.model.enums.SessionStatus;
import com.project.kineai.model.enums.Status;
import com.project.kineai.repository.ExerciseRepository;
import com.project.kineai.repository.PatientRepository;
import com.project.kineai.repository.PlanExerciseRepository;
import com.project.kineai.repository.RehabPlanRepository;
import com.project.kineai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final PatientRepository patientRepository;
    private final RehabPlanRepository rehabPlanRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final SessionRepository sessionRepository;
    private final ExerciseMapper exerciseMapper;

    // Mêmes jours que dans RehabPlanService —
    // ordre cohérent avec l'assignation initiale
    private static final SessionDay[] TRAINING_DAYS = {
            SessionDay.LUNDI,
            SessionDay.MERCREDI,
            SessionDay.VENDREDI
    };

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    // ✅ Exercices du PROCHAIN jour d'entraînement
    // dans la semaine courante du plan actif
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getMyExercises() {
        String email = SecurityContextHolder
                .getContext().getAuthentication().getName();

        Patient patient = patientRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException(
                        "Patient introuvable"));

        RehabPlan activePlan = rehabPlanRepository
                .findByPatientIdAndStatus(
                        patient.getId(), Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        "Aucun plan actif. Contactez"
                                + " votre kinésithérapeute."));

        int currentWeek = activePlan.getCurrentWeek();

        // Bornes de la semaine courante du plan
        LocalDate weekStart = activePlan.getStartDate()
                .plusDays((currentWeek - 1) * 7L);
        LocalDate weekEnd = weekStart.plusDays(7);

        long sessionsThisWeek = sessionRepository
                .countByRehabPlanIdAndSessionStatusAndStartTimeBetween(
                        activePlan.getId(),
                        SessionStatus.COMPLETED,
                        weekStart.atStartOfDay(),
                        weekEnd.atStartOfDay());

        if (sessionsThisWeek >= TRAINING_DAYS.length) {
            throw new RuntimeException(
                    "Vous avez terminé toutes les séances"
                            + " prévues cette semaine."
                            + " Revenez la semaine prochaine !");
        }

        SessionDay nextDay =
                TRAINING_DAYS[(int) sessionsThisWeek];

        List<PlanExercise> planExercises =
                planExerciseRepository
                        .findByRehabPlanIdAndWeekNumberAndDayOfWeek(
                                activePlan.getId(),
                                currentWeek, nextDay);

        if (planExercises.isEmpty()) {
            throw new RuntimeException(
                    "Aucun exercice prévu pour votre"
                            + " prochaine séance.");
        }

        return planExercises.stream()
                .map(PlanExercise::getExercise)
                .distinct()
                .map(exerciseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getByBodyZone(
            com.project.kineai.model.enums.BodyZone bodyZone) {
        return exerciseRepository
                .findByBodyZone(bodyZone)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }
}