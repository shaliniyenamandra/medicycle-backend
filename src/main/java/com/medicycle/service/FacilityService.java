package com.medicycle.service;

import com.medicycle.dto.response.FacilityResponse;
import com.medicycle.entity.Facility;
import com.medicycle.entity.User;
import com.medicycle.exception.ResourceNotFoundException;
import com.medicycle.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;

    @Transactional(readOnly = true)
    public FacilityResponse getProfile(User user) {
        Facility facility = facilityRepository.findByOwnerUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Facility profile not found"));
        return FacilityResponse.from(facility);
    }
}
