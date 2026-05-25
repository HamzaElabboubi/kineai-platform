package com.project.kineai.dto.response;

import com.project.kineai.model.enums.SessionDay;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePlanExerciseRequest {
    private UUID id;
    private UUID exerciseId;
    private String exerciseName;  // nom de l'exercice
    private Integer weekNumber;
    private SessionDay dayOfWeek;
    private Integer repsPrescribed;
    private Integer orderInSession;
}
