package com.medicycle.repository;

import com.medicycle.entity.Facility;
import com.medicycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByOwnerUser(User ownerUser);
    Optional<Facility> findByOwnerUserId(Long userId);
    boolean existsByLicenseNo(String licenseNo);
}
