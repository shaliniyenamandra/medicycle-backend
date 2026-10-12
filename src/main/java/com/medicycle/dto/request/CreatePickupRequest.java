package com.medicycle.dto.request;

import com.medicycle.enums.WasteType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreatePickupRequest {
    @NotNull(message = "Waste type is required")
    private WasteType wasteType;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @NotNull(message = "Pickup window start is required")
    private LocalDateTime pickupWindowStart;

    @NotNull(message = "Pickup window end is required")
    private LocalDateTime pickupWindowEnd;

    @NotBlank(message = "Pickup location is required")
    @Size(max = 500)
    private String pickupLocation;
}
