package com.medicycle.service;

import com.medicycle.dto.request.LoginRequest;
import com.medicycle.dto.request.RegisterRequest;
import com.medicycle.dto.response.AuthResponse;
import com.medicycle.entity.Facility;
import com.medicycle.entity.User;
import com.medicycle.enums.ApprovalStatus;
import com.medicycle.enums.Role;
import com.medicycle.exception.BusinessException;
import com.medicycle.repository.FacilityRepository;
import com.medicycle.repository.UserRepository;
import com.medicycle.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }
        if (request.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot self-register as ADMIN");
        }
        if (request.getRole() == Role.FACILITY) {
            if (request.getFacilityName() == null || request.getFacilityName().isBlank()) {
                throw new BusinessException("Facility name is required for facility accounts");
            }
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new BusinessException("License number is required for facility accounts");
            }
            if (request.getAddress() == null || request.getAddress().isBlank()) {
                throw new BusinessException("Address is required for facility accounts");
            }
            if (facilityRepository.existsByLicenseNo(request.getLicenseNumber())) {
                throw new BusinessException("License number already registered");
            }
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .approvalStatus(ApprovalStatus.PENDING)
            .active(false)
            .phone(request.getPhone())
            .build();
        user = userRepository.save(user);

        if (request.getRole() == Role.FACILITY) {
            Facility facility = Facility.builder()
                .ownerUser(user)
                .name(request.getFacilityName())
                .licenseNo(request.getLicenseNumber())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
            facilityRepository.save(facility);
        }

        auditService.log(user, com.medicycle.enums.AuditAction.REGISTER, "User registered: " + user.getEmail());

        return AuthResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .approvalStatus(user.getApprovalStatus())
            .active(user.isActive())
            .message("Registration successful. Please wait for admin approval.")
            .build();
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        auditService.log(user, com.medicycle.enums.AuditAction.LOGIN,
            "Login from IP: " + httpRequest.getRemoteAddr());

        return AuthResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .approvalStatus(user.getApprovalStatus())
            .active(user.isActive())
            .message("Login successful")
            .build();
    }
}
