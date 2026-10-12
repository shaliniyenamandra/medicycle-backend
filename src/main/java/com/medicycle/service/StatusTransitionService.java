package com.medicycle.service;

import com.medicycle.enums.PickupStatus;
import com.medicycle.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class StatusTransitionService {

    private static final Map<PickupStatus, Set<PickupStatus>> VALID_TRANSITIONS = Map.of(
        PickupStatus.CREATED, Set.of(PickupStatus.ASSIGNED),
        PickupStatus.ASSIGNED, Set.of(PickupStatus.COLLECTED),
        PickupStatus.COLLECTED, Set.of(PickupStatus.IN_TRANSIT),
        PickupStatus.IN_TRANSIT, Set.of(PickupStatus.PROCESSED),
        PickupStatus.PROCESSED, Set.of(PickupStatus.CLOSED),
        PickupStatus.CLOSED, Set.of()
    );

    /**
     * Validates a status transition.
     * @throws BusinessException if the transition is invalid
     */
    public void validate(PickupStatus from, PickupStatus to) {
        Set<PickupStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BusinessException(
                String.format("Invalid status transition: %s → %s. Allowed transitions from %s: %s",
                    from, to, from, allowed.isEmpty() ? "none" : allowed)
            );
        }
    }

    public boolean isTransitionValid(PickupStatus from, PickupStatus to) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
}
