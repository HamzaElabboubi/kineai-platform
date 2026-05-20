package com.project.kineai.dto.response;

<<<<<<< HEAD
import lombok.Builder;
=======
>>>>>>> 2b65152 (Create dto And mapper)
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
<<<<<<< HEAD
@Builder
=======
>>>>>>> 2b65152 (Create dto And mapper)
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
