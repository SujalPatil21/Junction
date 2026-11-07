package com.junction.repository;

import com.junction.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class JpaRepositoryIntegrationTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantMembershipRepository membershipRepository;

    @Autowired
    private RestaurantInventoryRepository inventoryRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private OperationalInterventionRepository interventionRepository;

    @Test
    @DisplayName("ProfileRepository: Save and find by clerk_user_id and email")
    void testProfileRepository() {
        String clerkId = "clerk_test_" + UUID.randomUUID();
        ProfileEntity profile = ProfileEntity.builder()
            .clerkUserId(clerkId)
            .email("test@junction.local")
            .name("Test User")
            .role(UserRole.ATTENDEE)
            .build();

        ProfileEntity saved = profileRepository.save(profile);
        assertThat(saved.getId()).isNotNull();

        Optional<ProfileEntity> found = profileRepository.findByClerkUserId(clerkId);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@junction.local");
        assertThat(profileRepository.existsByClerkUserId(clerkId)).isTrue();
    }

    @Test
    @DisplayName("RestaurantRepository: Save and query by zone")
    void testRestaurantRepository() {
        RestaurantEntity r = RestaurantEntity.builder()
            .id("R_TEST_" + UUID.randomUUID().toString().substring(0, 8))
            .name("Test Bistro")
            .zone("ZONE_A")
            .totalCapacity(120)
            .availableCovers(60)
            .isActive(true)
            .build();

        restaurantRepository.save(r);

        Optional<RestaurantEntity> found = restaurantRepository.findById(r.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Bistro");

        List<RestaurantEntity> zoneRestaurants = restaurantRepository.findByZoneAndIsActiveTrue("ZONE_A");
        assertThat(zoneRestaurants).isNotEmpty();
    }

    @Test
    @DisplayName("RestaurantMembershipRepository: Scoped unique relationship check")
    void testRestaurantMembershipRepository() {
        ProfileEntity profile = profileRepository.save(ProfileEntity.builder()
            .clerkUserId("clerk_mem_" + UUID.randomUUID())
            .email("mem@test.local")
            .role(UserRole.RESTAURANT_PARTNER)
            .build());

        RestaurantEntity restaurant = restaurantRepository.findById("R1").orElseThrow();

        RestaurantMembershipEntity membership = membershipRepository.save(RestaurantMembershipEntity.builder()
            .profile(profile)
            .restaurant(restaurant)
            .role(MembershipRole.MANAGER)
            .build());

        assertThat(membership.getId()).isNotNull();
        assertThat(membershipRepository.existsByProfileIdAndRestaurantId(profile.getId(), "R1")).isTrue();
    }

    @Test
    @DisplayName("AuditEventRepository: Immutable audit trail insertion and query")
    void testAuditEventRepository() {
        AuditEventEntity event = AuditEventEntity.builder()
            .id("AUDIT_TEST_" + UUID.randomUUID())
            .actorId("clerk_dev_organizer_001")
            .actorRole("ORGANIZER")
            .category("RECOMMENDATION_APPROVAL")
            .targetEntityType("RECOMMENDATION")
            .targetEntityId("REC_1")
            .changeSummary("Approved diversion")
            .isSimulatedScenario(false)
            .timestamp(Instant.now())
            .build();

        auditEventRepository.save(event);

        Optional<AuditEventEntity> found = auditEventRepository.findById(event.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getChangeSummary()).isEqualTo("Approved diversion");
    }

    @Test
    @DisplayName("OperationalInterventionRepository: Recommendation state lifecycle query")
    void testOperationalInterventionRepository() {
        OperationalInterventionEntity intervention = OperationalInterventionEntity.builder()
            .id("INT_TEST_" + UUID.randomUUID())
            .type("PEDESTRIAN_DIVERSION")
            .title("Divert via Marine Drive")
            .description("Pressure relief")
            .targetZoneId("ZONE_WANKHEDE")
            .status("PROPOSED")
            .urgency("HIGH")
            .requiresApproval(true)
            .rationale("High congestion")
            .confidenceScore(0.85)
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        interventionRepository.save(intervention);

        Optional<OperationalInterventionEntity> found = interventionRepository.findById(intervention.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("PROPOSED");

        List<OperationalInterventionEntity> active = interventionRepository.findByStatus("PROPOSED");
        assertThat(active).isNotEmpty();
    }
}
