package com.medicycle.service;

import com.medicycle.dto.request.CreatePickupRequest;
import com.medicycle.dto.request.ReschedulePickupRequest;
import com.medicycle.dto.request.VerifyQRRequest;
import com.medicycle.dto.response.*;
import com.medicycle.entity.*;
import com.medicycle.enums.*;
import com.medicycle.exception.BusinessException;
import com.medicycle.exception.ResourceNotFoundException;
import com.medicycle.exception.UnauthorizedException;
import com.medicycle.repository.*;
import com.medicycle.security.CustomUserDetails;
import com.medicycle.util.TraceCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickupService {

    private final PickupRequestRepository pickupRequestRepository;
    private final FacilityRepository facilityRepository;
    private final CollectionEventRepository collectionEventRepository;
    private final DisposalProofRepository disposalProofRepository;
    private final AuditLogRepository auditLogRepository;
    private final TraceCodeGenerator traceCodeGenerator;
    private final QRCodeService qrCodeService;
    private final StatusTransitionService statusTransitionService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @Transactional
    public PickupResponse createPickup(CreatePickupRequest request, User facilityUser) {
        if (request.getPickupWindowEnd().isBefore(request.getPickupWindowStart())) {
            throw new BusinessException("Pickup window end must be after start");
        }
        if (request.getPickupWindowStart().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new BusinessException("Pickup window start cannot be in the past");
        }

        Facility facility = facilityRepository.findByOwnerUserId(facilityUser.getId())
            .orElseThrow(() -> new BusinessException("No facility profile found for this user"));

        String traceCode = generateUniqueTraceCode();
        String qrBase64 = qrCodeService.generateBase64QR(traceCode);

        PickupRequest pickup = PickupRequest.builder()
            .facility(facility)
            .traceCode(traceCode)
            .wasteType(request.getWasteType())
            .weight(request.getWeight())
            .pickupWindowStart(request.getPickupWindowStart())
            .pickupWindowEnd(request.getPickupWindowEnd())
            .pickupLocation(request.getPickupLocation())
            .status(PickupStatus.CREATED)
            .qrCodeBase64(qrBase64)
            .build();

        pickup = pickupRequestRepository.save(pickup);
        auditService.log(facilityUser, AuditAction.PICKUP_CREATED, "PickupRequest", pickup.getId(),
            "Pickup created: " + traceCode, null);

        return PickupResponse.from(pickup);
    }

    @Transactional(readOnly = true)
    public List<PickupResponse> getFacilityPickups(User facilityUser) {
        Facility facility = facilityRepository.findByOwnerUserId(facilityUser.getId())
            .orElseThrow(() -> new BusinessException("No facility profile found"));
        return pickupRequestRepository.findByFacilityOrderByCreatedAtDesc(facility)
            .stream().map(PickupResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PickupDetailResponse getPickupDetail(Long id, User user) {
        PickupRequest pickup = findPickupById(id);
        validatePickupAccess(pickup, user);

        List<CollectionEventResponse> events = collectionEventRepository
            .findByPickupRequestOrderByCreatedAtAsc(pickup)
            .stream().map(CollectionEventResponse::from).collect(Collectors.toList());

        DisposalProofResponse proof = disposalProofRepository.findByPickupRequest(pickup)
            .map(dp -> DisposalProofResponse.from(dp, "http://localhost:8080"))
            .orElse(null);

        List<AuditLogResponse> auditLogs = auditLogRepository
            .findByEntityAndEntityIdOrderByTimestampAsc("PickupRequest", id)
            .stream().map(AuditLogResponse::from).collect(Collectors.toList());

        return PickupDetailResponse.builder()
            .pickup(PickupResponse.from(pickup))
            .collectionEvents(events)
            .disposalProof(proof)
            .auditLogs(auditLogs)
            .build();
    }


    @Transactional
    public PickupResponse reschedulePickup(Long id, ReschedulePickupRequest request, User facilityUser) {
        PickupRequest pickup = findPickupById(id);
        validatePickupAccess(pickup, facilityUser);
        if (pickup.getStatus() != PickupStatus.CREATED) {
            throw new BusinessException("Only CREATED pickup requests can be rescheduled");
        }
        if (!request.getPickupWindowEnd().isAfter(request.getPickupWindowStart())) {
            throw new BusinessException("Pickup window end must be after start");
        }
        if (request.getPickupWindowStart().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BusinessException("Pickup window start cannot be in the past");
        }
        pickup.setPickupWindowStart(request.getPickupWindowStart());
        pickup.setPickupWindowEnd(request.getPickupWindowEnd());
        if (request.getPickupLocation() != null && !request.getPickupLocation().isBlank()) {
            pickup.setPickupLocation(request.getPickupLocation().trim());
        }
        pickup = pickupRequestRepository.save(pickup);
        auditService.log(facilityUser, AuditAction.PICKUP_UPDATED, "PickupRequest", pickup.getId(),
            "Pickup rescheduled: " + pickup.getTraceCode(), null);
        return PickupResponse.from(pickup);
    }

    @Transactional
    public PickupResponse verifyQRForPickup(Long id, VerifyQRRequest request, User collector) {
        PickupRequest pickup = findPickupById(id);
        if (!pickup.getTraceCode().equalsIgnoreCase(request.getTraceCode().trim())) {
            auditService.logIndependent(collector, AuditAction.QR_SCAN_FAILED, "PickupRequest", pickup.getId(),
                "QR verification failed: trace code mismatch", null);
            throw new BusinessException("Trace code does not match this pickup");
        }
        return verifyQR(request, collector);
    }

    @Transactional(readOnly = true)
    public List<PickupResponse> getCollectorPickups(User collector) {
        return pickupRequestRepository.findByAssignedCollectorOrderByCreatedAtDesc(collector)
            .stream().map(PickupResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public PickupResponse collectorAcceptPickup(Long id, User collector) {
        PickupRequest pickup = findPickupById(id);
        if (pickup.getAssignedCollector() == null || !pickup.getAssignedCollector().getId().equals(collector.getId())) {
            throw new UnauthorizedException("You are not assigned to this pickup");
        }
        if (pickup.getStatus() != PickupStatus.ASSIGNED) {
            throw new BusinessException("Pickup must be in ASSIGNED status to accept");
        }
        auditService.log(collector, AuditAction.PICKUP_ACCEPTED, "PickupRequest", pickup.getId(),
            "Collector accepted pickup: " + pickup.getTraceCode(), null);
        return PickupResponse.from(pickup);
    }

    @Transactional
    public PickupResponse verifyQR(VerifyQRRequest request, User collector) {
        String traceCode = request.getTraceCode();
        PickupRequest pickup = pickupRequestRepository.findByTraceCode(traceCode)
            .orElse(null);

        if (pickup == null) {
            auditService.logIndependent(collector, AuditAction.QR_SCAN_FAILED, null, null,
                "QR verification failed: trace code not found: " + traceCode, null);
            throw new BusinessException("Invalid trace code: " + traceCode);
        }
        if (pickup.getAssignedCollector() == null || !pickup.getAssignedCollector().getId().equals(collector.getId())) {
            auditService.logIndependent(collector, AuditAction.QR_SCAN_FAILED, "PickupRequest", pickup.getId(),
                "QR scan failed: pickup not assigned to this collector: " + traceCode, null);
            throw new UnauthorizedException("This pickup is not assigned to you");
        }
        if (pickup.getStatus() != PickupStatus.ASSIGNED) {
            auditService.logIndependent(collector, AuditAction.QR_SCAN_FAILED, "PickupRequest", pickup.getId(),
                "QR scan failed: invalid status " + pickup.getStatus() + " for: " + traceCode, null);
            throw new BusinessException("Pickup must be in ASSIGNED status for QR verification. Current: " + pickup.getStatus());
        }

        statusTransitionService.validate(pickup.getStatus(), PickupStatus.COLLECTED);
        pickup.setStatus(PickupStatus.COLLECTED);
        pickup = pickupRequestRepository.save(pickup);

        CollectionEvent event = CollectionEvent.builder()
            .pickupRequest(pickup)
            .collector(collector)
            .scanTime(LocalDateTime.now())
            .location(request.getLocation())
            .eventStatus("COLLECTED")
            .notes(request.getNotes() != null ? request.getNotes() : "QR verified and waste collected")
            .build();
        collectionEventRepository.save(event);

        auditService.log(collector, AuditAction.QR_SCAN_SUCCESS, "PickupRequest", pickup.getId(),
            "QR verified and status changed to COLLECTED: " + traceCode, null);
        auditService.log(collector, AuditAction.STATUS_CHANGED, "PickupRequest", pickup.getId(),
            "Status: ASSIGNED → COLLECTED", null);

        return PickupResponse.from(pickup);
    }

    @Transactional
    public PickupResponse updateStatus(Long id, PickupStatus newStatus, String notes, User collector) {
        PickupRequest pickup = findPickupById(id);
        if (pickup.getAssignedCollector() == null || !pickup.getAssignedCollector().getId().equals(collector.getId())) {
            throw new UnauthorizedException("You are not assigned to this pickup");
        }
        // Collector can only move: COLLECTED→IN_TRANSIT, IN_TRANSIT→PROCESSED
        if (newStatus == PickupStatus.CLOSED) {
            throw new UnauthorizedException("Only admin can close a request");
        }

        PickupStatus oldStatus = pickup.getStatus();
        statusTransitionService.validate(oldStatus, newStatus);
        pickup.setStatus(newStatus);
        pickup = pickupRequestRepository.save(pickup);

        CollectionEvent event = CollectionEvent.builder()
            .pickupRequest(pickup)
            .collector(collector)
            .scanTime(LocalDateTime.now())
            .eventStatus(newStatus.name())
            .notes(notes != null ? notes : "Status updated to " + newStatus)
            .build();
        collectionEventRepository.save(event);

        auditService.log(collector, AuditAction.STATUS_CHANGED, "PickupRequest", pickup.getId(),
            "Status: " + oldStatus + " → " + newStatus, null);

        return PickupResponse.from(pickup);
    }

    // Admin methods
    @Transactional(readOnly = true)
    public List<PickupResponse> getAllPickups() {
        return pickupRequestRepository.findAll().stream()
            .map(PickupResponse::from).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PickupResponse> getUnassignedPickups() {
        return pickupRequestRepository.findByStatusAndAssignedCollectorIsNull(PickupStatus.CREATED).stream()
            .map(PickupResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public PickupResponse assignCollector(Long pickupId, Long collectorId, User admin) {
        PickupRequest pickup = findPickupById(pickupId);
        if (pickup.getStatus() != PickupStatus.CREATED) {
            throw new BusinessException("Pickup must be in CREATED status to assign a collector. Current: " + pickup.getStatus());
        }

        User collector = userRepository.findById(collectorId)
            .orElseThrow(() -> new ResourceNotFoundException("Collector not found"));
        if (collector.getRole() != Role.COLLECTOR) {
            throw new BusinessException("User is not a COLLECTOR");
        }
        if (!collector.isActive() || collector.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException("Collector is not active or approved");
        }

        pickup.setAssignedCollector(collector);
        statusTransitionService.validate(PickupStatus.CREATED, PickupStatus.ASSIGNED);
        pickup.setStatus(PickupStatus.ASSIGNED);
        pickup = pickupRequestRepository.save(pickup);

        auditService.log(admin, AuditAction.COLLECTOR_ASSIGNED, "PickupRequest", pickup.getId(),
            "Collector " + collector.getName() + " assigned to pickup " + pickup.getTraceCode(), null);
        auditService.log(admin, AuditAction.STATUS_CHANGED, "PickupRequest", pickup.getId(),
            "Status: CREATED → ASSIGNED", null);

        return PickupResponse.from(pickup);
    }

    @Transactional
    public PickupResponse closePickup(Long id, User admin) {
        PickupRequest pickup = findPickupById(id);
        if (pickup.getStatus() != PickupStatus.PROCESSED) {
            throw new BusinessException("Pickup must be in PROCESSED status to close. Current: " + pickup.getStatus());
        }
        if (!disposalProofRepository.existsByPickupRequestId(id)) {
            throw new BusinessException("Cannot close pickup without a disposal proof. Please upload proof first.");
        }
        statusTransitionService.validate(PickupStatus.PROCESSED, PickupStatus.CLOSED);
        pickup.setStatus(PickupStatus.CLOSED);
        pickup = pickupRequestRepository.save(pickup);

        auditService.log(admin, AuditAction.REQUEST_CLOSED, "PickupRequest", pickup.getId(),
            "Request closed: " + pickup.getTraceCode(), null);
        auditService.log(admin, AuditAction.STATUS_CHANGED, "PickupRequest", pickup.getId(),
            "Status: PROCESSED → CLOSED", null);

        return PickupResponse.from(pickup);
    }

    @Transactional(readOnly = true)
    public List<PickupResponse> getDelayedPickups() {
        return pickupRequestRepository.findDelayedRequests(
                LocalDateTime.now(),
                List.of(PickupStatus.CREATED, PickupStatus.ASSIGNED)
            ).stream().map(PickupResponse::from).collect(Collectors.toList());
    }

    private PickupRequest findPickupById(Long id) {
        return pickupRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pickup request not found: " + id));
    }

    private void validatePickupAccess(PickupRequest pickup, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() == Role.FACILITY) {
            Facility facility = facilityRepository.findByOwnerUserId(user.getId()).orElse(null);
            if (facility == null || !facility.getId().equals(pickup.getFacility().getId())) {
                throw new UnauthorizedException("This pickup does not belong to your facility");
            }
        }
        if (user.getRole() == Role.COLLECTOR) {
            if (pickup.getAssignedCollector() == null || !pickup.getAssignedCollector().getId().equals(user.getId())) {
                throw new UnauthorizedException("This pickup is not assigned to you");
            }
        }
    }

    private String generateUniqueTraceCode() {
        String code;
        int attempts = 0;
        do {
            code = traceCodeGenerator.generate();
            attempts++;
        } while (pickupRequestRepository.findByTraceCode(code).isPresent() && attempts < 10);
        return code;
    }
}
