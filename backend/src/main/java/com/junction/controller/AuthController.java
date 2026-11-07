package com.junction.controller;

import com.junction.entity.ProfileEntity;
import com.junction.entity.RestaurantMembershipEntity;
import com.junction.entity.UserRole;
import com.junction.repository.ProfileRepository;
import com.junction.repository.RestaurantMembershipRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Current authenticated identity & profile resolution")
public class AuthController {

    private final ProfileRepository profileRepository;
    private final RestaurantMembershipRepository membershipRepository;

    public AuthController(
        ProfileRepository profileRepository,
        RestaurantMembershipRepository membershipRepository
    ) {
        this.profileRepository = profileRepository;
        this.membershipRepository = membershipRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated profile and memberships")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        String clerkUserId = auth.getName();
        Optional<ProfileEntity> optProfile = profileRepository.findByClerkUserId(clerkUserId);
        ProfileEntity profile;
        if (optProfile.isEmpty()) {
            String email = null;
            String name = null;
            if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                email = jwt.getClaimAsString("email");
                name = jwt.getClaimAsString("name");
                if (name == null) {
                    name = jwt.getClaimAsString("preferred_username");
                }
            }
            if (email == null || email.isBlank()) {
                email = clerkUserId + "@clerk.local";
            }

            // Standard secure provisioning: All newly registered identities strictly default to ATTENDEE.
            // Elevated privileges (ORGANIZER, RESTAURANT_PARTNER) are granted exclusively through trusted
            // database administration (e.g., PostgreSQL migration seeds or administrator role assignment).
            profile = ProfileEntity.builder()
                .clerkUserId(clerkUserId)
                .email(email)
                .name(name != null ? name : "Attendee")
                .role(UserRole.ATTENDEE)
                .build();
            profile = profileRepository.save(profile);
        } else {
            profile = optProfile.get();
        }

        List<RestaurantMembershipEntity> memberships = membershipRepository.findByProfileId(profile.getId());

        List<Map<String, Object>> membershipDtos = memberships.stream()
            .map(m -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", m.getId().toString());
                map.put("restaurantId", m.getRestaurant().getId());
                map.put("restaurantName", m.getRestaurant().getName());
                map.put("role", m.getRole().name());
                return map;
            })
            .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("profile", Map.of(
            "id", profile.getId().toString(),
            "clerkUserId", profile.getClerkUserId(),
            "name", profile.getName() != null ? profile.getName() : "",
            "email", profile.getEmail(),
            "role", profile.getRole().name()
        ));
        response.put("memberships", membershipDtos);
        response.put("isOrganizer", profile.getRole() == UserRole.ORGANIZER);
        response.put("isRestaurantPartner", profile.getRole() == UserRole.RESTAURANT_PARTNER || !memberships.isEmpty());

        return ResponseEntity.ok(response);
    }
}
