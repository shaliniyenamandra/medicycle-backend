package com.medicycle.controller;

import com.medicycle.dto.request.AssignCollectorRequest;
import com.medicycle.dto.request.UpdateUserActiveRequest;
import com.medicycle.dto.response.*;
import com.medicycle.security.CustomUserDetails;
import com.medicycle.service.AdminService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final PickupService pickupService;
    private final DisposalProofService disposalProofService;

    @GetMapping("/dashboard")
    public DashboardStatsResponse dashboard() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/pending")
    public List<UserResponse> pendingUsers() {
        return adminService.getPendingUsers();
    }

    @GetMapping("/collectors")
    public List<UserResponse> collectors() {
        return adminService.getApprovedCollectors();
    }

    @GetMapping("/facilities")
    public List<FacilityResponse> facilities() {
        return adminService.getFacilities();
    }

    @PostMapping("/users/{id}/approve")
    public UserResponse approve(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails admin) {
        return adminService.approveUser(id, admin.getUser());
    }

    @PostMapping("/users/{id}/reject")
    public UserResponse reject(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails admin) {
        return adminService.rejectUser(id, admin.getUser());
    }

    @PatchMapping("/users/{id}/active")
    public UserResponse setActive(@PathVariable Long id,
                                  @RequestBody UpdateUserActiveRequest request,
                                  @AuthenticationPrincipal CustomUserDetails admin) {
        return adminService.setUserActive(id, request.isActive(), admin.getUser());
    }

    @GetMapping("/pickups")
    public List<PickupResponse> pickups() {
        return pickupService.getAllPickups();
    }

    @GetMapping("/pickups/delayed")
    public List<PickupResponse> delayed() {
        return pickupService.getDelayedPickups();
    }

    @GetMapping("/pickups/unassigned")
    public List<PickupResponse> unassigned() {
        return pickupService.getUnassignedPickups();
    }

    @GetMapping("/pickups/{id}")
    public PickupDetailResponse detail(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails admin) {
        return pickupService.getPickupDetail(id, admin.getUser());
    }

    @PostMapping("/pickups/{id}/assign")
    public PickupResponse assign(@PathVariable Long id,
                                 @Valid @RequestBody AssignCollectorRequest request,
                                 @AuthenticationPrincipal CustomUserDetails admin) {
        return pickupService.assignCollector(id, request.getCollectorId(), admin.getUser());
    }

    @PostMapping("/pickups/{id}/close")
    public PickupResponse close(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails admin) {
        return pickupService.closePickup(id, admin.getUser());
    }

    @PostMapping(value = "/pickups/{id}/proof", consumes = "multipart/form-data")
    public DisposalProofResponse uploadProof(@PathVariable Long id,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam String method,
                                             @RequestParam String processingFacility,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime processedAt,
                                             @AuthenticationPrincipal CustomUserDetails admin) {
        return disposalProofService.uploadProof(id, file, method, processingFacility,
            processedAt.toString(), admin.getUser());
    }

    @GetMapping("/audit")
    public List<AuditLogResponse> audit(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "100") int size) {
        return adminService.getAuditLogs(page, Math.min(size, 250));
    }
}
