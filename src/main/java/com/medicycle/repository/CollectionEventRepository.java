package com.medicycle.repository;

import com.medicycle.entity.CollectionEvent;
import com.medicycle.entity.PickupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CollectionEventRepository extends JpaRepository<CollectionEvent, Long> {
    List<CollectionEvent> findByPickupRequestOrderByCreatedAtAsc(PickupRequest pickupRequest);
    List<CollectionEvent> findByPickupRequestId(Long pickupRequestId);
    long countByEventStatusAndScanTimeBetween(String eventStatus, LocalDateTime start, LocalDateTime end);
}
