package com.medicycle.dto.response;

import com.medicycle.entity.DisposalProof;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DisposalProofResponse {
    private Long id;
    private Long pickupRequestId;
    private String method;
    private String processingFacility;
    private String certificateUrl;
    private String originalFilename;
    private LocalDateTime processedAt;
    private LocalDateTime uploadedAt;

    public static DisposalProofResponse from(DisposalProof dp, String baseUrl) {
        String certUrl = dp.getCertificatePath() != null 
            ? baseUrl + "/uploads/" + dp.getCertificatePath() 
            : null;
        return DisposalProofResponse.builder()
            .id(dp.getId())
            .pickupRequestId(dp.getPickupRequest().getId())
            .method(dp.getMethod())
            .processingFacility(dp.getProcessingFacility())
            .certificateUrl(certUrl)
            .originalFilename(dp.getOriginalFilename())
            .processedAt(dp.getProcessedAt())
            .uploadedAt(dp.getUploadedAt())
            .build();
    }
}
