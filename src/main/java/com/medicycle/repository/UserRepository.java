package com.medicycle.repository;

import com.medicycle.entity.User;
import com.medicycle.enums.ApprovalStatus;
import com.medicycle.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByApprovalStatus(ApprovalStatus status);
    List<User> findByRole(Role role);
    List<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus status);
    long countByApprovalStatus(ApprovalStatus status);
    long countByRole(Role role);
    long countByRoleAndActive(Role role, boolean active);
}
