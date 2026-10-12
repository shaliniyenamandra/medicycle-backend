package com.medicycle.config;

import com.medicycle.entity.*;
import com.medicycle.enums.*;
import com.medicycle.repository.*;
import com.medicycle.service.QRCodeService;
import com.medicycle.util.TraceCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DemoDataConfig {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final PickupRequestRepository pickupRepository;
    private final CollectionEventRepository eventRepository;
    private final DisposalProofRepository disposalProofRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final QRCodeService qrCodeService;
    private final TraceCodeGenerator traceCodeGenerator;

    @Bean
    @ConditionalOnProperty(name = "medicycle.seed-demo", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedDemoData() {
        return args -> {
            User admin = ensureUser("MediCycle Admin", "admin@medicycle.com", "Admin@123", Role.ADMIN, true, ApprovalStatus.APPROVED, "+919000000001");
            User facilityOwner = ensureUser("Dr. Ananya Rao", "greenvalley@hospital.com", "Facility@123", Role.FACILITY, true, ApprovalStatus.APPROVED, "+919000000002");
            User collector = ensureUser("Arjun Kumar", "john.collector@medicycle.com", "Collector@123", Role.COLLECTOR, true, ApprovalStatus.APPROVED, "+919000000003");
            ensureUser("Meera Collector", "meera.pending@medicycle.com", "Collector@123", Role.COLLECTOR, false, ApprovalStatus.PENDING, "+919000000004");

            Facility facility = facilityRepository.findByOwnerUserId(facilityOwner.getId()).orElseGet(() ->
                facilityRepository.save(Facility.builder()
                    .ownerUser(facilityOwner)
                    .name("Green Valley Diagnostics")
                    .licenseNo("TG-BMW-2026-0142")
                    .address("Banjara Hills, Hyderabad, Telangana")
                    .phone("+919000000002")
                    .build())
            );

            if (pickupRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();
                createPickup(facility, null, WasteType.SHARPS, 4.2,
                    now.plusHours(4), now.plusHours(7), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.CREATED, false);

                createPickup(facility, collector, WasteType.INFECTIOUS, 8.6,
                    now.plusHours(1), now.plusHours(3), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.ASSIGNED, false);

                createPickup(facility, collector, WasteType.PATHOLOGICAL, 3.4,
                    now.minusHours(3), now.minusHours(1), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.COLLECTED, true);

                createPickup(facility, collector, WasteType.PHARMACEUTICAL, 12.1,
                    now.minusHours(8), now.minusHours(6), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.IN_TRANSIT, true);

                createPickup(facility, collector, WasteType.CHEMICAL, 5.7,
                    now.minusDays(1), now.minusDays(1).plusHours(2), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.PROCESSED, true);

                PickupRequest closed = createPickup(facility, collector, WasteType.GENERAL_CLINICAL, 15.5,
                    now.minusDays(3), now.minusDays(3).plusHours(2), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.CLOSED, true);
                disposalProofRepository.save(DisposalProof.builder()
                    .pickupRequest(closed)
                    .method("Autoclave and authorized treatment")
                    .processingFacility("Hyderabad Biomedical Treatment Facility")
                    .processedAt(now.minusDays(3).plusHours(5))
                    .build());

                createPickup(facility, null, WasteType.PRESSURIZED, 2.8,
                    now.minusDays(1), now.minusHours(16), "Green Valley Diagnostics, Banjara Hills",
                    PickupStatus.CREATED, false);

                auditLogRepository.saveAll(List.of(
                    AuditLog.builder().user(admin).action(AuditAction.ACCOUNT_APPROVED).entity("User").entityId(facilityOwner.getId()).details("Demo facility approved").build(),
                    AuditLog.builder().user(admin).action(AuditAction.COLLECTOR_ASSIGNED).entity("PickupRequest").entityId(closed.getId()).details("Demo collector assignment").build(),
                    AuditLog.builder().user(collector).action(AuditAction.QR_SCAN_SUCCESS).entity("PickupRequest").entityId(closed.getId()).details("Demo QR verification").build(),
                    AuditLog.builder().user(admin).action(AuditAction.REQUEST_CLOSED).entity("PickupRequest").entityId(closed.getId()).details("Demo request closed with disposal proof").build()
                ));
            }
        };
    }

    private User ensureUser(String name, String email, String password, Role role, boolean active,
                            ApprovalStatus approvalStatus, String phone) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
            .name(name)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(role)
            .approvalStatus(approvalStatus)
            .active(active)
            .phone(phone)
            .build()));
    }

    private PickupRequest createPickup(Facility facility, User collector, WasteType type, double weight,
                                       LocalDateTime start, LocalDateTime end, String location,
                                       PickupStatus status, boolean addEvent) {
        String trace = traceCodeGenerator.generate();
        PickupRequest pickup = pickupRepository.save(PickupRequest.builder()
            .facility(facility)
            .assignedCollector(collector)
            .traceCode(trace)
            .wasteType(type)
            .weight(weight)
            .pickupWindowStart(start)
            .pickupWindowEnd(end)
            .pickupLocation(location)
            .status(status)
            .qrCodeBase64(qrCodeService.generateBase64QR(trace))
            .build());

        if (addEvent && collector != null && status.ordinal() >= PickupStatus.COLLECTED.ordinal()) {
            eventRepository.save(CollectionEvent.builder()
                .pickupRequest(pickup)
                .collector(collector)
                .scanTime(start.plusMinutes(30))
                .location(location)
                .eventStatus(status.name())
                .notes("Seeded demonstration event")
                .build());
        }
        return pickup;
    }
}
