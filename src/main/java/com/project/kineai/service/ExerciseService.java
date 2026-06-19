package com.project.kineai.service;

import com.project.kineai.dto.response.ExerciseResponse;
import com.project.kineai.mapper.ExerciseMapper;
import com.project.kineai.model.entity.Patient;
import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.model.enums.Status;
import com.project.kineai.repository.ExerciseRepository;
import com.project.kineai.repository.PatientRepository;

import com.project.kineai.repository.PlanExerciseRepository;
import com.project.kineai.repository.RehabPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final PatientRepository patientRepository;
    private final RehabPlanRepository rehabPlanRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    // ✅ Corrigé — exercices du PLAN ACTIF, pas du profil
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getMyExercises() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

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

        List<PlanExercise> planExercises =
                planExerciseRepository
                        .findByRehabPlanIdAndWeekNumber(
                                activePlan.getId(),
                                activePlan.getCurrentWeek());

        // Dédupliquer — un exercice peut apparaître
        // plusieurs fois (plusieurs jours) dans la
        // même semaine, on ne veut le proposer qu'une
        // fois dans la liste de sélection patient
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