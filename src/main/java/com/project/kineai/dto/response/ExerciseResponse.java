package com.project.kineai.dto.response;

import com.project.kineai.model.enums.Level;
import com.project.kineai.model.enums.Pathology;
import lombok.Data;

import java.util.UUID;

@Data
public class ExerciseResponse {
    private UUID id;
    private String name;
    private Pathology bodyZone;
    private Integer targetAngle;
    private Integer toleranceDeg;
    private Integer repsTarget;
    private Level difficultyLevel;
    private String mediapipeJoints;
}
