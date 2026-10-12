package com.medicycle.dto.response;

import com.medicycle.entity.CollectionEvent;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionEventResponse {
    private Long id;
    private Long pickupRequestId;
    private Long collectorId;
    private String collectorName;
    private LocalDateTime scanTime;
    private String location;
    private String eventStatus;
    private String notes;
    private LocalDateTime createdAt;

    public static CollectionEventResponse from(CollectionEvent ce) {
        return CollectionEventResponse.builder()
            .id(ce.getId())
            .pickupRequestId(ce.getPickupRequest().getId())
            .collectorId(ce.getCollector().getId())
            .collectorName(ce.getCollector().getName())
            .scanTime(ce.getScanTime())
            .location(ce.getLocation())
            .eventStatus(ce.getEventStatus())
            .notes(ce.getNotes())
            .createdAt(ce.getCreatedAt())
            .build();
    }
}
