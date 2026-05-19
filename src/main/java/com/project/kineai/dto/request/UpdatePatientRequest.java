package com.project.kineai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePatientRequest {
    @NotBlank
    private String fullName;
    @NotNull
    @Min(1) @Max(120) private Integer age;
}
