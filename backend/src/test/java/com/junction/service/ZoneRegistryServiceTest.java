package com.junction.service;

import com.junction.dto.ZoneCoverageProfileDto;
import com.junction.dto.ZoneDefinitionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneRegistryServiceTest {

    private ZoneRegistryService zoneRegistry;

    @BeforeEach
    void setUp() {
        zoneRegistry = new ZoneRegistryService();
    }

    @Test
    @DisplayName("All 7 canonical zones are registered in the zone registry")
    void testGetAllZones() {
        List<ZoneDefinitionDto> allZones = zoneRegistry.getAllZones();
        assertThat(allZones).hasSize(7);
        assertThat(allZones).extracting(ZoneDefinitionDto::getId)
            .containsExactlyInAnyOrder(
                "ZONE_WANKHEDE",
                "ZONE_CHURCHGATE",
                "ZONE_TAXI_STAGING",
                "ZONE_MARINE_LINES",
                "ZONE_CSMT",
                "ZONE_HOTELS_SOUTH",
                "ZONE_DADAR"
            );
    }

    @Test
    @DisplayName("Lookup by ID returns correct zone definition")
    void testGetZoneById() {
        Optional<ZoneDefinitionDto> zoneOpt = zoneRegistry.getZoneById("ZONE_WANKHEDE");
        assertThat(zoneOpt).isPresent();
        assertThat(zoneOpt.get().getNominalPedestrianCapacity()).isEqualTo(37000);
    }

    @Test
    @DisplayName("Coverage profiles enforce data honesty tiers")
    void testCoverageProfilesHonesty() {
        Map<String, ZoneCoverageProfileDto> profiles = zoneRegistry.getAllCoverageProfiles();
        assertThat(profiles).containsKey("ZONE_WANKHEDE");
        assertThat(profiles.get("ZONE_WANKHEDE").getCoverageStatus()).isEqualTo("FULL");
        assertThat(profiles.get("ZONE_WANKHEDE").isCanTriggerOperationalRecommendations()).isTrue();

        assertThat(profiles).containsKey("ZONE_CSMT");
        assertThat(profiles.get("ZONE_CSMT").getCoverageStatus()).isEqualTo("UNINSTRUMENTED");
        assertThat(profiles.get("ZONE_CSMT").isCanTriggerOperationalRecommendations()).isFalse();
    }
}
