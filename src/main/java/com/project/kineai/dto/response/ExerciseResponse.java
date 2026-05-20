package com.project.kineai.dto.response;

<<<<<<< HEAD
import com.project.kineai.model.enums.BodyZone;
import com.project.kineai.model.enums.Level;
=======
import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Pathology;
>>>>>>> 2b65152 (Create dto And mapper)
import lombok.Data;

import java.util.UUID;

@Data
public class ExerciseResponse {
    private UUID id;
    private String name;
<<<<<<< HEAD
    private BodyZone bodyZone;
=======
    private Pathology bodyZone;
>>>>>>> 2b65152 (Create dto And mapper)
    private Integer targetAngle;
    private Integer toleranceDeg;
    private Integer repsTarget;
    private Level difficultyLevel;
    private String mediapipeJoints;
}
