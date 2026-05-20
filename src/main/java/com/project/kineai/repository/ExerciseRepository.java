package com.project.kineai.repository;

import com.project.kineai.model.entity.Exercise;
<<<<<<< HEAD
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.model.enums.Level;
=======
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Pathology;
>>>>>>> 2b65152 (Create dto And mapper)
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
<<<<<<< HEAD
    List<Exercise> findByBodyZoneAndDifficultyLevel(BodyZone bodyZone, Level level);
    List<Exercise> findByBodyZone(BodyZone bodyZone);
=======
    List<Exercise> findByBodyZoneAndDifficultyLevel(Pathology bodyZone, Level level);
    List<Exercise> findByBodyZone(Pathology bodyZone);
>>>>>>> 2b65152 (Create dto And mapper)
}
