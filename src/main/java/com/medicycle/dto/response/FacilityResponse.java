package com.medicycle.dto.response;

import com.medicycle.entity.Facility;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FacilityResponse {
    private Long id;
    private Long ownerUserId;
    private String ownerName;
    private String name;
    private String licenseNo;
    private String address;
    private String phone;
    private LocalDateTime createdAt;

    public static FacilityResponse from(Facility facility) {
        return FacilityResponse.builder()
            .id(facility.getId())
            .ownerUserId(facility.getOwnerUser().getId())
            .ownerName(facility.getOwnerUser().getName())
            .name(facility.getName())
            .licenseNo(facility.getLicenseNo())
            .address(facility.getAddress())
            .phone(facility.getPhone())
            .createdAt(facility.getCreatedAt())
            .build();
    }
}
