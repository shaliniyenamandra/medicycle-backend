package com.medicycle.repository;

import com.medicycle.entity.AuditLog;
import com.medicycle.enums.AuditAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    List<AuditLog> findByEntityAndEntityIdOrderByTimestampAsc(String entity, Long entityId);
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);
    List<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:userId IS NULL OR al.user.id = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:startDate IS NULL OR al.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR al.timestamp <= :endDate) " +
           "ORDER BY al.timestamp DESC")
    List<AuditLog> findByFilters(
        @Param("userId") Long userId,
        @Param("action") AuditAction action,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
