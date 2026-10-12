package com.medicycle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ReportSummaryResponse {
    private long count;
    private double totalWeight;
    private Map<String, Long> statusTotals;
    private List<PickupResponse> pickups;
}
