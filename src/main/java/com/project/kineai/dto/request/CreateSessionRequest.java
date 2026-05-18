package com.project.kineai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSessionRequest {
    @NotNull
    private UUID exerciseId;
    private UUID planId;
}
