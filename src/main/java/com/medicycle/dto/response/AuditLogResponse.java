package com.medicycle.dto.response;

import com.medicycle.entity.AuditLog;
import com.medicycle.enums.AuditAction;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private AuditAction action;
    private String entity;
    private Long entityId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String details;

    public static AuditLogResponse from(AuditLog al) {
        return AuditLogResponse.builder()
            .id(al.getId())
            .userId(al.getUser() != null ? al.getUser().getId() : null)
            .userName(al.getUser() != null ? al.getUser().getName() : null)
            .userEmail(al.getUser() != null ? al.getUser().getEmail() : null)
            .action(al.getAction())
            .entity(al.getEntity())
            .entityId(al.getEntityId())
            .timestamp(al.getTimestamp())
            .ipAddress(al.getIpAddress())
            .details(al.getDetails())
            .build();
    }
}
