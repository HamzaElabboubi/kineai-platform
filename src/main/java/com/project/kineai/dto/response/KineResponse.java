package com.project.kineai.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class KineResponse {
    private UUID id;
    private String fullName;
    private String speciality;
    private Boolean validated;
    private Integer patientCount;
}
