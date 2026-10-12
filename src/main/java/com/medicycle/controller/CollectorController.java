package com.medicycle.controller;

import com.medicycle.dto.request.StatusUpdateRequest;
import com.medicycle.dto.request.VerifyQRRequest;
import com.medicycle.dto.response.DisposalProofResponse;
import com.medicycle.dto.response.PickupDetailResponse;
import com.medicycle.dto.response.PickupResponse;
import com.medicycle.security.CustomUserDetails;
import com.medicycle.service.DisposalProofService;
import com.medicycle.service.PickupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {
    private final PickupService pickupService;
    private final DisposalProofService disposalProofService;

    @GetMapping("/pickups")
    public List<PickupResponse> pickups(@AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.getCollectorPickups(user.getUser());
    }

    @GetMapping("/pickups/{id}")
    public PickupDetailResponse detail(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.getPickupDetail(id, user.getUser());
    }

    @PostMapping("/pickups/{id}/accept")
    public PickupResponse accept(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.collectorAcceptPickup(id, user.getUser());
    }

    @PostMapping("/pickups/{id}/verify")
    public PickupResponse verify(@PathVariable Long id,
                                 @Valid @RequestBody VerifyQRRequest request,
                                 @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.verifyQRForPickup(id, request, user.getUser());
    }

    @PostMapping("/pickups/{id}/status")
    public PickupResponse updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody StatusUpdateRequest request,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.updateStatus(id, request.getStatus(), request.getNotes(), user.getUser());
    }

    @PostMapping(value = "/pickups/{id}/proof", consumes = "multipart/form-data")
    public DisposalProofResponse uploadProof(@PathVariable Long id,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam String method,
                                             @RequestParam String processingFacility,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime processedAt,
                                             @AuthenticationPrincipal CustomUserDetails user) {
        return disposalProofService.uploadProof(id, file, method, processingFacility,
            processedAt.toString(), user.getUser());
    }
}
