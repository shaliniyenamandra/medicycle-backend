package com.medicycle.dto.response;

import com.medicycle.entity.User;
import com.medicycle.enums.ApprovalStatus;
import com.medicycle.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private ApprovalStatus approvalStatus;
    private boolean active;
    private String phone;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .approvalStatus(user.getApprovalStatus())
            .active(user.isActive())
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
