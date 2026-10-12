package com.medicycle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "disposal_proofs", indexes = {
    @Index(name = "idx_dp_pickup_request", columnList = "pickup_request_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_request_id", nullable = false, unique = true)
    private PickupRequest pickupRequest;

    @Column(nullable = false, length = 200)
    private String method;

    @Column(nullable = false, length = 200)
    private String processingFacility;

    @Column(length = 500)
    private String certificatePath;

    @Column(length = 200)
    private String originalFilename;

    @Column(length = 100)
    private String contentType;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
