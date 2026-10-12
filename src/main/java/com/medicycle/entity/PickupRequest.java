package com.medicycle.entity;

import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.WasteType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pickup_requests", indexes = {
    @Index(name = "idx_pr_trace_code", columnList = "trace_code"),
    @Index(name = "idx_pr_status", columnList = "status"),
    @Index(name = "idx_pr_facility", columnList = "facility_id"),
    @Index(name = "idx_pr_collector", columnList = "assigned_collector_id"),
    @Index(name = "idx_pr_pickup_window_end", columnList = "pickup_window_end")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_collector_id")
    private User assignedCollector;

    @Column(name = "trace_code", nullable = false, unique = true, length = 50)
    private String traceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WasteType wasteType;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "pickup_window_start", nullable = false)
    private LocalDateTime pickupWindowStart;

    @Column(name = "pickup_window_end", nullable = false)
    private LocalDateTime pickupWindowEnd;

    @Column(name = "pickup_location", nullable = false, length = 500)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PickupStatus status = PickupStatus.CREATED;

    @Column(name = "qr_code_base64", columnDefinition = "LONGTEXT")
    private String qrCodeBase64;

    @OneToMany(mappedBy = "pickupRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CollectionEvent> collectionEvents = new ArrayList<>();

    @OneToOne(mappedBy = "pickupRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DisposalProof disposalProof;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
