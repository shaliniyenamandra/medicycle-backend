package com.medicycle.dto.response;

import com.medicycle.entity.PickupRequest;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.WasteType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PickupResponse {
    private Long id;
    private String traceCode;
    private WasteType wasteType;
    private String wasteTypeDisplay;
    private Double weight;
    private LocalDateTime pickupWindowStart;
    private LocalDateTime pickupWindowEnd;
    private String pickupLocation;
    private PickupStatus status;
    private String qrCodeBase64;
    private Long facilityId;
    private String facilityName;
    private String facilityAddress;
    private Long collectorId;
    private String collectorName;
    private String collectorEmail;
    private boolean delayed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PickupResponse from(PickupRequest pr) {
        boolean isDelayed = pr.getPickupWindowEnd().isBefore(LocalDateTime.now())
            && (pr.getStatus() == PickupStatus.CREATED || pr.getStatus() == PickupStatus.ASSIGNED);

        return PickupResponse.builder()
            .id(pr.getId())
            .traceCode(pr.getTraceCode())
            .wasteType(pr.getWasteType())
            .wasteTypeDisplay(pr.getWasteType().getDisplayName())
            .weight(pr.getWeight())
            .pickupWindowStart(pr.getPickupWindowStart())
            .pickupWindowEnd(pr.getPickupWindowEnd())
            .pickupLocation(pr.getPickupLocation())
            .status(pr.getStatus())
            .qrCodeBase64(pr.getQrCodeBase64())
            .facilityId(pr.getFacility().getId())
            .facilityName(pr.getFacility().getName())
            .facilityAddress(pr.getFacility().getAddress())
            .collectorId(pr.getAssignedCollector() != null ? pr.getAssignedCollector().getId() : null)
            .collectorName(pr.getAssignedCollector() != null ? pr.getAssignedCollector().getName() : null)
            .collectorEmail(pr.getAssignedCollector() != null ? pr.getAssignedCollector().getEmail() : null)
            .delayed(isDelayed)
            .createdAt(pr.getCreatedAt())
            .updatedAt(pr.getUpdatedAt())
            .build();
    }
}
