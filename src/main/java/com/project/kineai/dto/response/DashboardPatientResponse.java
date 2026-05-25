package com.project.kineai.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardPatientResponse {
    private PatientResponse profile;
    private RehabPlanResponse activePlan;
    private Integer totalSessions;
    private BigDecimal averageScore;
    private Integer streakCount;
    private Integer totalXp;
    private List<BadgeResponse> badges;
    private List<SessionResponse> recentSessions;
    private Integer progressionPct;
}
