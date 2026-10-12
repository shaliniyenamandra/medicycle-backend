package com.medicycle.service;

import com.medicycle.dto.response.DisposalProofResponse;
import com.medicycle.entity.DisposalProof;
import com.medicycle.entity.PickupRequest;
import com.medicycle.entity.User;
import com.medicycle.enums.AuditAction;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.Role;
import com.medicycle.exception.BusinessException;
import com.medicycle.exception.ResourceNotFoundException;
import com.medicycle.exception.UnauthorizedException;
import com.medicycle.repository.DisposalProofRepository;
import com.medicycle.repository.PickupRequestRepository;
import com.medicycle.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DisposalProofService {

    private final DisposalProofRepository disposalProofRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final FileStorageUtil fileStorageUtil;
    private final AuditService auditService;
    private final StatusTransitionService statusTransitionService;

    @Transactional
    public DisposalProofResponse uploadProof(Long pickupId, MultipartFile file,
                                              String method, String processingFacilityName,
                                              String processedAtStr, User uploader) {
        PickupRequest pickup = pickupRequestRepository.findById(pickupId)
            .orElseThrow(() -> new ResourceNotFoundException("Pickup request not found: " + pickupId));

        if (pickup.getStatus() != PickupStatus.PROCESSED && pickup.getStatus() != PickupStatus.IN_TRANSIT) {
            throw new BusinessException("Disposal proof can only be uploaded when status is IN_TRANSIT or PROCESSED");
        }
        if (uploader.getRole() == Role.COLLECTOR &&
            (pickup.getAssignedCollector() == null || !pickup.getAssignedCollector().getId().equals(uploader.getId()))) {
            throw new UnauthorizedException("You are not authorized to upload proof for this pickup");
        }

        if (disposalProofRepository.existsByPickupRequestId(pickupId)) {
            throw new BusinessException("Disposal proof already exists for this pickup");
        }

        String storedFilename = fileStorageUtil.store(file);

        LocalDateTime processedAt = LocalDateTime.parse(processedAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        DisposalProof proof = DisposalProof.builder()
                .pickupRequest(pickup)
                .method(method)
                .processingFacility(processingFacilityName)
                .certificatePath(storedFilename)
                .originalFilename(file.getOriginalFilename())
                .processedAt(processedAt)
                .build();

        proof = disposalProofRepository.save(proof);

        if (pickup.getStatus() == PickupStatus.IN_TRANSIT) {
            statusTransitionService.validate(PickupStatus.IN_TRANSIT, PickupStatus.PROCESSED);
            pickup.setStatus(PickupStatus.PROCESSED);
            pickupRequestRepository.save(pickup);
            auditService.log(uploader, AuditAction.STATUS_CHANGED, "PickupRequest", pickup.getId(),
                    "Status: IN_TRANSIT → PROCESSED", null);
        }

        auditService.log(uploader, AuditAction.DISPOSAL_PROOF_UPLOADED, "PickupRequest", pickup.getId(),
                "Disposal proof uploaded", null);

        return DisposalProofResponse.from(proof, "http://localhost:8080");
    }
}
