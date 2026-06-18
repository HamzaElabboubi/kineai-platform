package com.project.kineai.dto.request;

import com.project.kineai.model.enums.SessionDay;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignExerciseRequest {

    @NotNull
    private UUID planId;

    @NotNull
    private UUID exerciseId;

    @NotNull
    @Min(1)
    @Max(4)
    private Integer weekNumber;

    @NotNull
    private SessionDay dayOfWeek;

    @NotNull
    @Min(1)
    private Integer repsPrescribed;

    private Integer orderInSession;
}