package com.project.kineai.repository;

import com.project.kineai.model.entity.PlanExercise;
import com.project.kineai.model.enums.SessionDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanExerciseRepository extends JpaRepository<PlanExercise, UUID> {
    // Exercices d'un plan pour une semaine donnée
    List<PlanExercise> findByRehabPlanIdAndWeekNumber(
            UUID rehabPlanId, Integer weekNumber);

    // Exercices d'un plan pour un jour donné
    List<PlanExercise> findByRehabPlanIdAndWeekNumberAndDayOfWeek(
            UUID rehabPlanId, Integer weekNumber, SessionDay dayOfWeek);

    // Tous les exercices d'un plan
    List<PlanExercise> findByRehabPlanIdOrderByWeekNumberAscOrderInSessionAsc(
            UUID rehabPlanId);

    // Supprimer tous les exercices d'un plan
    void deleteByRehabPlanId(UUID rehabPlanId);


}
