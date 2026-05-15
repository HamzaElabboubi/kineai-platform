package com.project.kineai.Repository;

import com.project.kineai.model.entity.PlanExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanExerciseRepository extends JpaRepository<PlanExercise, UUID> {


}
