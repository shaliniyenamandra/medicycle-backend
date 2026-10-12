package com.medicycle.service;

import com.medicycle.enums.PickupStatus;
import com.medicycle.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTransitionServiceTest {
    private final StatusTransitionService service = new StatusTransitionService();

    @Test
    void allowsCanonicalTransitions() {
        assertTrue(service.isTransitionValid(PickupStatus.CREATED, PickupStatus.ASSIGNED));
        assertTrue(service.isTransitionValid(PickupStatus.ASSIGNED, PickupStatus.COLLECTED));
        assertTrue(service.isTransitionValid(PickupStatus.COLLECTED, PickupStatus.IN_TRANSIT));
        assertTrue(service.isTransitionValid(PickupStatus.IN_TRANSIT, PickupStatus.PROCESSED));
        assertTrue(service.isTransitionValid(PickupStatus.PROCESSED, PickupStatus.CLOSED));
    }

    @Test
    void rejectsIllegalJump() {
        assertThrows(BusinessException.class,
            () -> service.validate(PickupStatus.CREATED, PickupStatus.CLOSED));
        assertThrows(BusinessException.class,
            () -> service.validate(PickupStatus.ASSIGNED, PickupStatus.PROCESSED));
    }
}
