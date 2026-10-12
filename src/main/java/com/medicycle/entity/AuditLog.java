package com.medicycle.entity;

import com.medicycle.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_al_user", columnList = "user_id"),
    @Index(name = "idx_al_action", columnList = "action"),
    @Index(name = "idx_al_timestamp", columnList = "timestamp"),
    @Index(name = "idx_al_entity", columnList = "entity, entity_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(length = 100)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(length = 50)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String details;
}
