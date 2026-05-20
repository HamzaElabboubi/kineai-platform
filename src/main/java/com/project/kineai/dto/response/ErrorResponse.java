package com.project.kineai.dto.response;

<<<<<<< HEAD
import lombok.Builder;
=======
>>>>>>> 2b65152 (Create dto And mapper)
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
<<<<<<< HEAD
@Builder
=======
>>>>>>> 2b65152 (Create dto And mapper)
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;
}
