package com.project.kineai.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompleteSessionRequest {
    @NotNull
    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal finalScore;
    @NotNull @Min(0) private Integer repsCompleted;
    @NotBlank(message = "Les angles articulaires sont obligatoires")
    private String jointAngles; // JSON résumé final
}
