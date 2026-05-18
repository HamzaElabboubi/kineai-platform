package com.project.kineai.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardKineResponse {
    private Integer totalPatients;
    private Long pendingAlerts;
    private List<PatientResponse> patients;
    private List<AlertResponse> recentAlerts;
}
