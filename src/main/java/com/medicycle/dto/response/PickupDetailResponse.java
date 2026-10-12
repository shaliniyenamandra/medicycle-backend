package com.medicycle.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PickupDetailResponse {
    private PickupResponse pickup;
    private List<CollectionEventResponse> collectionEvents;
    private DisposalProofResponse disposalProof;
    private List<AuditLogResponse> auditLogs;
}
