package com.medicycle.service;

import com.medicycle.dto.response.*;
import com.medicycle.entity.User;
import com.medicycle.enums.*;
import com.medicycle.exception.BusinessException;
import com.medicycle.exception.ResourceNotFoundException;
import com.medicycle.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final CollectionEventRepository collectionEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final DisposalProofRepository disposalProofRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING)
            .stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<UserResponse> getApprovedCollectors() {
        return userRepository.findByRoleAndApprovalStatus(Role.COLLECTOR, ApprovalStatus.APPROVED)
            .stream().filter(User::isActive).map(UserResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> getFacilities() {
        return facilityRepository.findAll().stream()
            .map(FacilityResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse approveUser(Long userId, User admin) {
        User user = findUser(userId);
        if (user.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new BusinessException("User is already approved");
        }
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);
        user = userRepository.save(user);
        auditService.log(admin, AuditAction.ACCOUNT_APPROVED, "User", userId,
            "Approved user: " + user.getEmail(), null);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse rejectUser(Long userId, User admin) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot reject admin user");
        }
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setActive(false);
        user = userRepository.save(user);
        auditService.log(admin, AuditAction.ACCOUNT_REJECTED, "User", userId,
            "Rejected user: " + user.getEmail(), null);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse setUserActive(Long userId, boolean active, User admin) {
        User user = findUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot deactivate admin");
        }
        user.setActive(active);
        user = userRepository.save(user);
        AuditAction action = active ? AuditAction.ACCOUNT_ACTIVATED : AuditAction.ACCOUNT_DEACTIVATED;
        auditService.log(admin, action, "User", userId,
            (active ? "Activated" : "Deactivated") + " user: " + user.getEmail(), null);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        long totalPickups = pickupRequestRepository.count();
        long pendingApprovals = userRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long inTransit = pickupRequestRepository.countByStatus(PickupStatus.IN_TRANSIT);
        long closedThisMonth = pickupRequestRepository.countByStatusAndUpdatedAtAfter(PickupStatus.CLOSED, startOfMonth);
        long totalFacilities = userRepository.countByRole(Role.FACILITY);
        long totalCollectors = userRepository.countByRole(Role.COLLECTOR);
        long awaitingProof = pickupRequestRepository.countByStatus(PickupStatus.PROCESSED);

        List<com.medicycle.entity.PickupRequest> delayed = pickupRequestRepository.findDelayedRequests(
            LocalDateTime.now(), List.of(PickupStatus.CREATED, PickupStatus.ASSIGNED)
        );
        long delayedCount = delayed.size();

        long activePickups = pickupRequestRepository.countByStatusIn(
            List.of(PickupStatus.ASSIGNED, PickupStatus.COLLECTED, PickupStatus.IN_TRANSIT, PickupStatus.PROCESSED)
        );

        long collectedToday = collectionEventRepository.countByEventStatusAndScanTimeBetween(
            "COLLECTED", startOfDay, startOfDay.plusDays(1)
        );

        // Status distribution
        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (PickupStatus s : PickupStatus.values()) {
            statusDist.put(s.name(), pickupRequestRepository.countByStatus(s));
        }

        // Waste type distribution
        Map<String, Long> wasteTypeDist = new LinkedHashMap<>();
        List<com.medicycle.entity.PickupRequest> allPickups = pickupRequestRepository.findAll();
        for (com.medicycle.entity.PickupRequest p : allPickups) {
            wasteTypeDist.merge(p.getWasteType().name(), 1L, Long::sum);
        }

        return DashboardStatsResponse.builder()
            .totalPickups(totalPickups)
            .activePickups(activePickups)
            .pendingApprovals(pendingApprovals)
            .delayedPickups(delayedCount)
            .collectedToday(collectedToday)
            .inTransit(inTransit)
            .awaitingProof(awaitingProof)
            .closedThisMonth(closedThisMonth)
            .totalFacilities(totalFacilities)
            .totalCollectors(totalCollectors)
            .statusDistribution(statusDist)
            .wasteTypeDistribution(wasteTypeDist)
            .build();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
            .stream().map(AuditLogResponse::from).collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
