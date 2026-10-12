package com.medicycle.dto.response;

import com.medicycle.enums.ApprovalStatus;
import com.medicycle.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private ApprovalStatus approvalStatus;
    private boolean active;
    private String message;
}
