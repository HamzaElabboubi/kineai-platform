package com.project.kineai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreatePlanRequest {
    @NotNull
    private UUID patientId;

    private LocalDate startDate;
}
