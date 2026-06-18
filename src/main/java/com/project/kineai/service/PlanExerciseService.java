package com.project.kineai.service;

import com.project.kineai.Repository.PlanExerciseRepository;
import com.project.kineai.dto.request.AssignExerciseRequest;
import com.project.kineai.dto.response.PlanExerciseResponse;
import com.project.kineai.mapper.PlanExerciseMapper;
import com.project.kineai.model.entity.Exercise;
import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.entity.RehabPlan;
import com.project.kineai.repository.ExerciseRepository;
import com.project.kineai.repository.RehabPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanExerciseService {

    private final PlanExerciseRepository planExerciseRepository;
    private final RehabPlanRepository rehabPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final PlanExerciseMapper planExerciseMapper;

    // ── Assigner un exercice au plan ──────────
    @Transactional
    public PlanExerciseResponse assignExercise(
            AssignExerciseRequest request) {

        RehabPlan plan = rehabPlanRepository
                .findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException(
                        "Plan de rééducation introuvable"));

        Exercise exercise = exerciseRepository
                .findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException(
                        "Exercice introuvable"));

        boolean exists = planExerciseRepository
                .existsByRehabPlanIdAndExerciseIdAndWeekNumberAndDayOfWeek(
                        plan.getId(), exercise.getId(),
                        request.getWeekNumber(),
                        request.getDayOfWeek());

        if (exists) {
            throw new RuntimeException(
                    "Cet exercice est déjà assigné pour"
                            + " cette semaine et ce jour.");
        }

        int order = request.getOrderInSession() != null
                ? request.getOrderInSession()
                : (int) planExerciseRepository
                .findByRehabPlanIdAndWeekNumber(
                        plan.getId(),
                        request.getWeekNumber()).size() + 1;

        PlanExercise pe = PlanExercise.builder()
                .rehabPlan(plan)
                .exercise(exercise)
                .weekNumber(request.getWeekNumber())
                .dayOfWeek(request.getDayOfWeek())
                .repsPrescribed(request.getRepsPrescribed())
                .orderInSession(order)
                .build();

        pe = planExerciseRepository.save(pe);
        log.info("Exercice {} assigné au plan {} "
                        + "(semaine {}, {})",
                exercise.getName(), plan.getId(),
                request.getWeekNumber(),
                request.getDayOfWeek());

        return planExerciseMapper.toResponse(pe);
    }

    // ── Liste des exercices d'un plan ─────────
    @Transactional(readOnly = true)
    public List<PlanExerciseResponse> getPlanExercises(
            UUID planId) {
        List<PlanExercise> list = planExerciseRepository
                .findByRehabPlanIdOrderByWeekNumberAscOrderInSessionAsc(
                        planId);
        return planExerciseMapper.toResponseList(list);
    }

    // ── Exercices de la semaine courante ──────
    @Transactional(readOnly = true)
    public List<PlanExerciseResponse> getCurrentWeekExercises(
            UUID planId, Integer currentWeek) {
        List<PlanExercise> list = planExerciseRepository
                .findByRehabPlanIdAndWeekNumber(
                        planId, currentWeek);
        return planExerciseMapper.toResponseList(list);
    }

    // ── Retirer un exercice du plan ───────────
    @Transactional
    public void removeExercise(UUID planExerciseId) {
        planExerciseRepository.deleteById(planExerciseId);
        log.info("Exercice retiré du plan : {}",
                planExerciseId);
    }
}