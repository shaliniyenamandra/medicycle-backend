package com.medicycle.controller;

import com.medicycle.dto.request.CreatePickupRequest;
import com.medicycle.dto.request.ReschedulePickupRequest;
import com.medicycle.dto.response.FacilityResponse;
import com.medicycle.dto.response.PickupDetailResponse;
import com.medicycle.dto.response.PickupResponse;
import com.medicycle.security.CustomUserDetails;
import com.medicycle.service.FacilityService;
import com.medicycle.service.PickupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;
    private final PickupService pickupService;

    @GetMapping("/profile")
    public FacilityResponse profile(@AuthenticationPrincipal CustomUserDetails user) {
        return facilityService.getProfile(user.getUser());
    }

    @GetMapping("/pickups")
    public List<PickupResponse> pickups(@AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.getFacilityPickups(user.getUser());
    }

    @PostMapping("/pickups")
    public ResponseEntity<PickupResponse> create(@Valid @RequestBody CreatePickupRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pickupService.createPickup(request, user.getUser()));
    }

    @GetMapping("/pickups/{id}")
    public PickupDetailResponse detail(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.getPickupDetail(id, user.getUser());
    }

    @PatchMapping("/pickups/{id}/reschedule")
    public PickupResponse reschedule(@PathVariable Long id,
                                     @Valid @RequestBody ReschedulePickupRequest request,
                                     @AuthenticationPrincipal CustomUserDetails user) {
        return pickupService.reschedulePickup(id, request, user.getUser());
    }
}
