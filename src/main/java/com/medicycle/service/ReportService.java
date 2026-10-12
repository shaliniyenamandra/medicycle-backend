package com.medicycle.service;

import com.medicycle.dto.response.PickupResponse;
import com.medicycle.dto.response.ReportSummaryResponse;
import com.medicycle.entity.PickupRequest;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.WasteType;
import com.medicycle.repository.PickupRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PickupRequestRepository pickupRequestRepository;

    @Transactional(readOnly = true)
    public ReportSummaryResponse getReport(Long facilityId, Long collectorId, PickupStatus status,
                                           WasteType wasteType, LocalDateTime startDate, LocalDateTime endDate) {
        List<PickupRequest> rows = pickupRequestRepository.findByFilters(
            facilityId, collectorId, status, wasteType, startDate, endDate
        );
        Map<String, Long> statusTotals = new LinkedHashMap<>();
        double totalWeight = 0;
        for (PickupRequest row : rows) {
            statusTotals.merge(row.getStatus().name(), 1L, Long::sum);
            if (row.getWeight() != null) totalWeight += row.getWeight();
        }
        return ReportSummaryResponse.builder()
            .count(rows.size())
            .totalWeight(totalWeight)
            .statusTotals(statusTotals)
            .pickups(rows.stream().map(PickupResponse::from).collect(Collectors.toList()))
            .build();
    }

    @Transactional(readOnly = true)
    public String exportCsv(Long facilityId, Long collectorId, PickupStatus status,
                            WasteType wasteType, LocalDateTime startDate, LocalDateTime endDate) {
        ReportSummaryResponse report = getReport(facilityId, collectorId, status, wasteType, startDate, endDate);
        StringBuilder csv = new StringBuilder();
        csv.append("Trace Code,Facility,Collector,Waste Type,Weight,Pickup Window Start,Pickup Window End,Status,Location\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (PickupResponse p : report.getPickups()) {
            csv.append(escape(p.getTraceCode())).append(',')
               .append(escape(p.getFacilityName())).append(',')
               .append(escape(p.getCollectorName())).append(',')
               .append(escape(p.getWasteTypeDisplay())).append(',')
               .append(p.getWeight() == null ? "" : p.getWeight()).append(',')
               .append(p.getPickupWindowStart() == null ? "" : p.getPickupWindowStart().format(fmt)).append(',')
               .append(p.getPickupWindowEnd() == null ? "" : p.getPickupWindowEnd().format(fmt)).append(',')
               .append(p.getStatus()).append(',')
               .append(escape(p.getPickupLocation())).append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        String safe = value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
