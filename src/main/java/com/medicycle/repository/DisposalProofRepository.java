package com.medicycle.repository;

import com.medicycle.entity.DisposalProof;
import com.medicycle.entity.PickupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisposalProofRepository extends JpaRepository<DisposalProof, Long> {
    Optional<DisposalProof> findByPickupRequest(PickupRequest pickupRequest);
    Optional<DisposalProof> findByPickupRequestId(Long pickupRequestId);
    boolean existsByPickupRequestId(Long pickupRequestId);
}
