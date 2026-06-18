package com.project.kineai.Repository;

import com.project.kineai.model.entity.PlanExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanExerciseRepository
        extends JpaRepository<PlanExercise, UUID> {

    List<PlanExercise> findByRehabPlanIdOrderByWeekNumberAscOrderInSessionAsc(
            UUID rehabPlanId);

    List<PlanExercise> findByRehabPlanIdAndWeekNumber(
            UUID rehabPlanId, Integer weekNumber);

    boolean existsByRehabPlanIdAndExerciseIdAndWeekNumberAndDayOfWeek(
            UUID rehabPlanId, UUID exerciseId,
            Integer weekNumber,
            com.project.kineai.model.enums.SessionDay dayOfWeek);
}