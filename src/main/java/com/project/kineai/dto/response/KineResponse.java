package com.project.kineai.dto.response;

import lombok.Data;
<<<<<<< HEAD
import lombok.Getter;
import lombok.Setter;
=======
>>>>>>> 2b65152 (Create dto And mapper)

import java.util.UUID;

@Data
public class KineResponse {
    private UUID id;
    private String fullName;
    private String speciality;
    private Boolean validated;
    private Integer patientCount;
}
