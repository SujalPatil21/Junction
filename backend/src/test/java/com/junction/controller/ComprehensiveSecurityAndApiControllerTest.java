package com.junction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junction.dto.InventoryLogRequestDto;
import com.junction.dto.NormalizedObservationDto;
import com.junction.dto.PartnerInventoryRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ComprehensiveSecurityAndApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── 1. PUBLIC ENDPOINTS (No Authentication) ─────────────────────

    @Test
    @DisplayName("Public: GET /api/health returns 200 OK with service metadata")
    void testPublicHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.service").value("junction-backend"));
    }

    @Test
    @DisplayName("Public: GET /api/scenarios returns 200 OK with scenario list for web & mobile")
    void testPublicScenarios() throws Exception {
        mockMvc.perform(get("/api/scenarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.activeScenario").exists())
            .andExpect(jsonPath("$.scenarios").isArray());
    }

    // ── 2. UNAUTHENTICATED CALLS TO PROTECTED ENDPOINTS (Expect 401) ──

    @Test
    @DisplayName("Security: GET /api/zones returns 401 when unauthenticated")
    void testUnauthenticatedZonesRejected() throws Exception {
        mockMvc.perform(get("/api/zones"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: GET /api/recommendations returns 401 when unauthenticated")
    void testUnauthenticatedRecommendationsRejected() throws Exception {
        mockMvc.perform(get("/api/recommendations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: GET /api/forecast returns 401 when unauthenticated")
    void testUnauthenticatedForecastRejected() throws Exception {
        mockMvc.perform(get("/api/forecast"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: GET /api/predictions returns 401 when unauthenticated")
    void testUnauthenticatedPredictionsRejected() throws Exception {
        mockMvc.perform(get("/api/predictions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: GET /api/hotspots returns 401 when unauthenticated")
    void testUnauthenticatedHotspotsRejected() throws Exception {
        mockMvc.perform(get("/api/hotspots"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: GET /api/audit returns 401 when unauthenticated")
    void testUnauthenticatedAuditRejected() throws Exception {
        mockMvc.perform(get("/api/audit"))
            .andExpect(status().isUnauthorized());
    }

    // ── 3. EDGE DEVICE AUTHENTICATION (POST /api/observations) ──────

    @Test
    @DisplayName("Edge: Ingestion rejected with 401 if Authorization header is missing")
    void testEdgeIngestionNoAuthHeader() throws Exception {
        Map<String, Object> body = Map.of("observations", List.of());
        mockMvc.perform(post("/api/observations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge: Ingestion rejected with 401 if edge secret is invalid")
    void testEdgeIngestionInvalidSecret() throws Exception {
        Map<String, Object> body = Map.of("observations", List.of());
        mockMvc.perform(post("/api/observations")
                .header("Authorization", "Bearer wrong-secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge: Ingestion accepted with 200 when valid edge secret is provided")
    void testEdgeIngestionValidSecret() throws Exception {
        NormalizedObservationDto obs = NormalizedObservationDto.builder()
            .id("OBS_TEST_SEC_1")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .metricType("CROWD_COUNT")
            .value(1200.0)
            .confidence(0.9)
            .qualityStatus("FRESH")
            .build();

        Map<String, Object> body = Map.of("observations", List.of(obs));
        mockMvc.perform(post("/api/observations")
                .header("Authorization", "Bearer dev-local-secret-not-for-production")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acceptedCount").value(1))
            .andExpect(jsonPath("$.quarantinedCount").value(0));
    }

    // ── 4. RBAC AUTHORIZATION CHECKS (ROLE_ORGANIZER vs ROLE_ATTENDEE) ──

    @Test
    @DisplayName("RBAC: Authenticated Attendee CAN view zones")
    void testAttendeeCanViewZones() throws Exception {
        mockMvc.perform(get("/api/zones")
                .with(jwt().jwt(j -> j.subject("clerk_attendee_001")).authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("RBAC: Authenticated Attendee CANNOT approve recommendations (403 Forbidden)")
    void testAttendeeCannotApproveRecommendation() throws Exception {
        mockMvc.perform(post("/api/recommendations/REC_1/approve")
                .with(jwt().jwt(j -> j.subject("clerk_attendee_001")).authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rationale\": \"Unauthorized attempt\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Authenticated Attendee CANNOT set active scenario (403 Forbidden)")
    void testAttendeeCannotSetScenario() throws Exception {
        mockMvc.perform(post("/api/scenarios")
                .with(jwt().jwt(j -> j.subject("clerk_attendee_001")).authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarioId\": \"POST_EVENT_SURGE\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Authorized Organizer CAN set active scenario (200 OK)")
    void testOrganizerCanSetScenario() throws Exception {
        mockMvc.perform(post("/api/scenarios")
                .with(jwt().jwt(j -> j.subject("clerk_dev_organizer_001")).authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarioId\": \"POST_EVENT_SURGE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.activeScenario").value("POST_EVENT_SURGE"));
    }

    @Test
    @DisplayName("RBAC: Authorized Organizer CAN query the audit trail (200 OK)")
    void testOrganizerCanViewAuditTrail() throws Exception {
        mockMvc.perform(get("/api/audit")
                .with(jwt().jwt(j -> j.subject("clerk_dev_organizer_001")).authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("RBAC: Attendee CANNOT query audit trail (403 Forbidden)")
    void testAttendeeCannotViewAuditTrail() throws Exception {
        mockMvc.perform(get("/api/audit")
                .with(jwt().jwt(j -> j.subject("clerk_attendee_001")).authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE"))))
            .andExpect(status().isForbidden());
    }

    // ── 5. RESTAURANT PARTNER SCOPED INVENTORY MUTATIONS ───────────

    @Test
    @DisplayName("Partner: Partner with membership for R1 CAN record live inventory")
    void testPartnerCanRecordInventoryForAssignedRestaurant() throws Exception {
        PartnerInventoryRequestDto dto = PartnerInventoryRequestDto.builder()
            .restaurantId("R1")
            .availableTables(10)
            .availableCovers(25)
            .expectedCovers(50)
            .notes("Peak match crowd update")
            .build();

        mockMvc.perform(post("/api/partner/inventory")
                .with(jwt().jwt(j -> j.subject("clerk_dev_partner_001")).authorities(new SimpleGrantedAuthority("ROLE_RESTAURANT_PARTNER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.restaurant.availableCovers").value(25));
    }

    @Test
    @DisplayName("Partner: Organizer CAN record inventory for any restaurant without membership")
    void testOrganizerCanRecordInventoryForAnyRestaurant() throws Exception {
        PartnerInventoryRequestDto dto = PartnerInventoryRequestDto.builder()
            .restaurantId("R1")
            .availableTables(12)
            .availableCovers(35)
            .expectedCovers(40)
            .build();

        mockMvc.perform(post("/api/partner/inventory")
                .with(jwt().jwt(j -> j.subject("clerk_dev_organizer_001")).authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ── 6. AUTH PROFILE RESOLUTION (/api/auth/me) ──────────────────

    @Test
    @DisplayName("Auth: GET /api/auth/me resolves profile and domain role from PostgreSQL")
    void testGetAuthMeOrganizer() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(j -> j.subject("clerk_dev_organizer_001")).authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.profile.clerkUserId").value("clerk_dev_organizer_001"))
            .andExpect(jsonPath("$.profile.role").value("ORGANIZER"))
            .andExpect(jsonPath("$.isOrganizer").value(true));
    }

    @Test
    @DisplayName("Auth: GET /api/auth/me resolves partner profile and scoped memberships")
    void testGetAuthMePartner() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(j -> j.subject("clerk_dev_partner_001")).authorities(new SimpleGrantedAuthority("ROLE_RESTAURANT_PARTNER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.profile.clerkUserId").value("clerk_dev_partner_001"))
            .andExpect(jsonPath("$.memberships").isArray())
            .andExpect(jsonPath("$.memberships[0].restaurantId").value("R1"))
            .andExpect(jsonPath("$.isRestaurantPartner").value(true));
    }

    @Test
    @DisplayName("Auth: GET /api/auth/me auto-provisions unknown users strictly with ATTENDEE role")
    void testGetAuthMeNewUserDefaultsToAttendee() throws Exception {
        String newUserId = "clerk_new_attendee_" + UUID.randomUUID();
        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(j -> j.subject(newUserId)).authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.profile.clerkUserId").value(newUserId))
            .andExpect(jsonPath("$.profile.role").value("ATTENDEE"))
            .andExpect(jsonPath("$.isOrganizer").value(false));
    }

    @Test
    @DisplayName("Auth: Forged client metadata claiming ORGANIZER cannot escalate unprovisioned user")
    void testNewUserCannotEscalateViaMetadata() throws Exception {
        String attackerId = "clerk_attacker_" + UUID.randomUUID();
        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(j -> j.subject(attackerId).claim("role", "ORGANIZER").claim("metadata", Map.of("role", "ORGANIZER")))
                    .authorities(new SimpleGrantedAuthority("ROLE_ATTENDEE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.role").value("ATTENDEE"))
            .andExpect(jsonPath("$.isOrganizer").value(false));
    }
}
