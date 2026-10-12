package com.medicycle.service;

import com.medicycle.entity.AuditLog;
import com.medicycle.entity.User;
import com.medicycle.enums.AuditAction;
import com.medicycle.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User user, AuditAction action, String entity, Long entityId, String details, String ipAddress) {
        auditLogRepository.save(build(user, action, entity, entityId, details, ipAddress));
    }

    @Transactional
    public void log(User user, AuditAction action, String details) {
        log(user, action, null, null, details, null);
    }

    /**
     * Used for rejected/failed actions that should still be recorded even when the
     * caller's transaction subsequently rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIndependent(User user, AuditAction action, String entity, Long entityId, String details, String ipAddress) {
        auditLogRepository.save(build(user, action, entity, entityId, details, ipAddress));
    }

    private AuditLog build(User user, AuditAction action, String entity, Long entityId, String details, String ipAddress) {
        return AuditLog.builder()
            .user(user)
            .action(action)
            .entity(entity)
            .entityId(entityId)
            .details(details)
            .ipAddress(ipAddress)
            .build();
    }
}
