package com.medicycle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collection_events", indexes = {
    @Index(name = "idx_ce_pickup_request", columnList = "pickup_request_id"),
    @Index(name = "idx_ce_collector", columnList = "collector_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_request_id", nullable = false)
    private PickupRequest pickupRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", nullable = false)
    private User collector;

    @Column(nullable = false)
    private LocalDateTime scanTime;

    @Column(length = 300)
    private String location;

    @Column(nullable = false, length = 100)
    private String eventStatus;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
