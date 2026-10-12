package com.medicycle.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyQRRequest {
    @NotBlank(message = "Trace code is required")
    private String traceCode;
    private String location;
    private String notes;
}
