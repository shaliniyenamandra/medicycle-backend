package com.medicycle.controller;

import com.medicycle.dto.response.ReportSummaryResponse;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.WasteType;
import com.medicycle.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public ReportSummaryResponse report(
        @RequestParam(required = false) Long facilityId,
        @RequestParam(required = false) Long collectorId,
        @RequestParam(required = false) PickupStatus status,
        @RequestParam(required = false) WasteType wasteType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return reportService.getReport(facilityId, collectorId, status, wasteType, startDate, endDate);
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(
        @RequestParam(required = false) Long facilityId,
        @RequestParam(required = false) Long collectorId,
        @RequestParam(required = false) PickupStatus status,
        @RequestParam(required = false) WasteType wasteType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        String csv = reportService.exportCsv(facilityId, collectorId, status, wasteType, startDate, endDate);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicycle-compliance-report.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
