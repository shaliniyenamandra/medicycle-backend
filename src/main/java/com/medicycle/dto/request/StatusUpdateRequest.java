package com.medicycle.dto.request;

import com.medicycle.enums.PickupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private PickupStatus status;
    private String notes;
}
