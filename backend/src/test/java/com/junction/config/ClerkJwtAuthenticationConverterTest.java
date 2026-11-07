package com.junction.config;

import com.junction.entity.ProfileEntity;
import com.junction.entity.RestaurantEntity;
import com.junction.entity.RestaurantMembershipEntity;
import com.junction.entity.UserRole;
import com.junction.repository.ProfileRepository;
import com.junction.repository.RestaurantMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClerkJwtAuthenticationConverterTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RestaurantMembershipRepository membershipRepository;

    private ClerkJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ClerkJwtAuthenticationConverter(profileRepository, membershipRepository);
    }

    private Jwt createMockJwt(String subject, Map<String, Object> claims) {
        return new Jwt(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims != null ? claims : Map.of("sub", subject)
        );
    }

    @Test
    @DisplayName("Authoritative resolution: PostgreSQL ORGANIZER profile grants ROLE_ORGANIZER")
    void testOrganizerRoleFromDatabase() {
        String clerkId = "user_organizer_123";
        ProfileEntity profile = ProfileEntity.builder()
            .id(UUID.randomUUID())
            .clerkUserId(clerkId)
            .email("organizer@junction.local")
            .role(UserRole.ORGANIZER)
            .build();

        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(profile));

        Jwt jwt = createMockJwt(clerkId, Map.of("sub", clerkId));
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth).isNotNull();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).contains("ROLE_ORGANIZER");
    }

    @Test
    @DisplayName("Least privilege: Unknown Clerk user defaults to ROLE_ATTENDEE")
    void testUnknownUserDefaultsToAttendee() {
        String clerkId = "user_unknown_999";
        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.empty());

        Jwt jwt = createMockJwt(clerkId, Map.of("sub", clerkId));
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth).isNotNull();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactly("ROLE_ATTENDEE");
    }

    @Test
    @DisplayName("Privilege escalation prevention: Token with forged metadata cannot override DB ATTENDEE role")
    void testForgedMetadataCannotEscalatePrivileges() {
        String clerkId = "user_attacker_001";
        ProfileEntity profile = ProfileEntity.builder()
            .id(UUID.randomUUID())
            .clerkUserId(clerkId)
            .email("attacker@junction.local")
            .role(UserRole.ATTENDEE)
            .build();

        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(profile));

        // Malicious token claiming metadata.role = ORGANIZER
        Map<String, Object> forgedClaims = new HashMap<>();
        forgedClaims.put("sub", clerkId);
        forgedClaims.put("metadata", Map.of("role", "ORGANIZER"));

        Jwt jwt = createMockJwt(clerkId, forgedClaims);
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth).isNotNull();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactly("ROLE_ATTENDEE");
        assertThat(authorities).doesNotContain("ROLE_ORGANIZER");
    }

    @Test
    @DisplayName("Restaurant partner: Grants ROLE_RESTAURANT_PARTNER and scoped membership authorities")
    void testRestaurantPartnerWithMemberships() {
        String clerkId = "user_partner_456";
        UUID profileId = UUID.randomUUID();
        ProfileEntity profile = ProfileEntity.builder()
            .id(profileId)
            .clerkUserId(clerkId)
            .email("partner@trishna.local")
            .role(UserRole.RESTAURANT_PARTNER)
            .build();

        RestaurantEntity restaurant = RestaurantEntity.builder().id("R1").name("Trishna").build();
        RestaurantMembershipEntity membership = RestaurantMembershipEntity.builder()
            .id(UUID.randomUUID())
            .profile(profile)
            .restaurant(restaurant)
            .build();

        when(profileRepository.findByClerkUserId(clerkId)).thenReturn(Optional.of(profile));
        when(membershipRepository.findByProfileId(profileId)).thenReturn(List.of(membership));

        Jwt jwt = createMockJwt(clerkId, Map.of("sub", clerkId));
        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth).isNotNull();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).contains("ROLE_RESTAURANT_PARTNER", "RESTAURANT_MEMBER_R1");
    }
}
