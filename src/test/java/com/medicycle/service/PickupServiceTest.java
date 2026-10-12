package com.medicycle.service;

import com.medicycle.entity.PickupRequest;
import com.medicycle.entity.User;
import com.medicycle.enums.ApprovalStatus;
import com.medicycle.enums.PickupStatus;
import com.medicycle.enums.Role;
import com.medicycle.exception.BusinessException;
import com.medicycle.repository.*;
import com.medicycle.util.TraceCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PickupServiceTest {
    private PickupRequestRepository pickupRepo;
    private DisposalProofRepository proofRepo;
    private PickupService service;

    @BeforeEach
    void setUp() {
        pickupRepo = mock(PickupRequestRepository.class);
        proofRepo = mock(DisposalProofRepository.class);
        service = new PickupService(
            pickupRepo,
            mock(FacilityRepository.class),
            mock(CollectionEventRepository.class),
            proofRepo,
            mock(AuditLogRepository.class),
            mock(TraceCodeGenerator.class),
            mock(QRCodeService.class),
            new StatusTransitionService(),
            mock(AuditService.class),
            mock(UserRepository.class)
        );
    }

    @Test
    void cannotCloseProcessedPickupWithoutProof() {
        PickupRequest pickup = PickupRequest.builder().id(10L).status(PickupStatus.PROCESSED).traceCode("MC-X").build();
        User admin = User.builder().id(1L).role(Role.ADMIN).approvalStatus(ApprovalStatus.APPROVED).active(true).build();
        when(pickupRepo.findById(10L)).thenReturn(Optional.of(pickup));
        when(proofRepo.existsByPickupRequestId(10L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.closePickup(10L, admin));
        verify(pickupRepo, never()).save(any());
    }
}
