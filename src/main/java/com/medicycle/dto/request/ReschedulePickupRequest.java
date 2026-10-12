package com.medicycle.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReschedulePickupRequest {
    @NotNull(message = "Pickup window start is required")
    private LocalDateTime pickupWindowStart;

    @NotNull(message = "Pickup window end is required")
    private LocalDateTime pickupWindowEnd;

    @Size(max = 500)
    private String pickupLocation;
}
