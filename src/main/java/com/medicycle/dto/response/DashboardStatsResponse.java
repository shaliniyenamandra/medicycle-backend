package com.medicycle.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalPickups;
    private long activePickups;
    private long pendingApprovals;
    private long delayedPickups;
    private long collectedToday;
    private long inTransit;
    private long awaitingProof;
    private long closedThisMonth;
    private long totalFacilities;
    private long totalCollectors;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> wasteTypeDistribution;
}
