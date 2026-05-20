package com.project.kineai.dto.response;

<<<<<<< HEAD
import lombok.Builder;
=======
>>>>>>> 2b65152 (Create dto And mapper)
import lombok.Data;

import java.util.List;

@Data
<<<<<<< HEAD
@Builder
=======
>>>>>>> 2b65152 (Create dto And mapper)
public class DashboardKineResponse {
    private Integer totalPatients;
    private Long pendingAlerts;
    private List<PatientResponse> patients;
    private List<AlertResponse> recentAlerts;
}
