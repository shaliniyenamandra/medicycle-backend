package com.medicycle.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignCollectorRequest {
    @NotNull(message = "Collector ID is required")
    private Long collectorId;
}
