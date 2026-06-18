package com.project.kineai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalPatients;
    private long totalKines;
    private long validatedKines;
    private long pendingKines;
}