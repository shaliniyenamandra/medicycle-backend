package com.medicycle.repository;

import com.medicycle.entity.Facility;
import com.medicycle.entity.PickupRequest;
import com.medicycle.entity.User;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {
    Optional<PickupRequest> findByTraceCode(String traceCode);
    List<PickupRequest> findByFacility(Facility facility);
    List<PickupRequest> findByFacilityOrderByCreatedAtDesc(Facility facility);
    List<PickupRequest> findByAssignedCollector(User collector);
    List<PickupRequest> findByAssignedCollectorOrderByCreatedAtDesc(User collector);
    List<PickupRequest> findByStatus(PickupStatus status);
    List<PickupRequest> findByStatusIn(List<PickupStatus> statuses);
    List<PickupRequest> findByAssignedCollectorAndStatus(User collector, PickupStatus status);
    List<PickupRequest> findByAssignedCollectorAndStatusIn(User collector, List<PickupStatus> statuses);

    // Delayed: pickup window end has passed but status is not yet COLLECTED or beyond
    @Query("SELECT pr FROM PickupRequest pr WHERE pr.pickupWindowEnd < :now AND pr.status IN :statuses")
    List<PickupRequest> findDelayedRequests(@Param("now") LocalDateTime now, @Param("statuses") List<PickupStatus> statuses);

    // Report queries
    @Query("SELECT pr FROM PickupRequest pr WHERE " +
           "(:facilityId IS NULL OR pr.facility.id = :facilityId) AND " +
           "(:collectorId IS NULL OR pr.assignedCollector.id = :collectorId) AND " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:wasteType IS NULL OR pr.wasteType = :wasteType) AND " +
           "(:startDate IS NULL OR pr.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR pr.createdAt <= :endDate) " +
           "ORDER BY pr.createdAt DESC")
    List<PickupRequest> findByFilters(
        @Param("facilityId") Long facilityId,
        @Param("collectorId") Long collectorId,
        @Param("status") PickupStatus status,
        @Param("wasteType") WasteType wasteType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    long countByStatus(PickupStatus status);
    long countByStatusIn(List<PickupStatus> statuses);

    List<PickupRequest> findByStatusAndAssignedCollectorIsNull(PickupStatus status);

    @Query("SELECT pr FROM PickupRequest pr WHERE pr.createdAt >= :since ORDER BY pr.createdAt DESC")
    List<PickupRequest> findRecentPickups(@Param("since") LocalDateTime since);

    long countByStatusAndUpdatedAtAfter(PickupStatus status, LocalDateTime since);
}
