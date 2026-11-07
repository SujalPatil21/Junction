package com.junction.service;

import com.junction.dto.NormalizedObservationDto;
import com.junction.dto.ZoneDefinitionDto;
import com.junction.dto.ZoneStateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensorFusionServiceTest {

    private SensorFusionService sensorFusionService;
    private ZoneRegistryService zoneRegistry;

    @BeforeEach
    void setUp() {
        DeviceRegistryService deviceRegistry = new DeviceRegistryService();
        zoneRegistry = new ZoneRegistryService();
        sensorFusionService = new SensorFusionService(deviceRegistry, zoneRegistry);
    }

    @Test
    @DisplayName("Fuse all zones returns states for all 7 canonical operational zones")
    void testFuseAllZones() {
        List<ZoneStateDto> states = sensorFusionService.fuseAllZones(List.of());
        assertThat(states).hasSize(7);
        assertThat(states).extracting(ZoneStateDto::getId)
            .contains("ZONE_WANKHEDE", "ZONE_CHURCHGATE", "ZONE_CSMT", "ZONE_DADAR");
    }

    @Test
    @DisplayName("Zone pressure is properly updated from high crowd observation")
    void testFuseZoneWithCrowdObservation() {
        ZoneDefinitionDto wankhede = zoneRegistry.getZoneById("ZONE_WANKHEDE").orElseThrow();
        NormalizedObservationDto obs = NormalizedObservationDto.builder()
            .id("OBS_TEST_1")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .metricType("CROWD_COUNT")
            .value(3600.0)
            .confidence(0.95)
            .qualityStatus("FRESH")
            .build();

        ZoneStateDto state = sensorFusionService.fuseZone(wankhede, List.of(obs));
        assertThat(state).isNotNull();
        assertThat(state.getPressure()).isGreaterThan(50);
        assertThat(state.getConfidence()).isGreaterThan(0.8);
        assertThat(state.getContributingSensors()).anyMatch(s -> s.contains("DEV_CCTV_WANKHEDE_01"));
    }

    @Test
    @DisplayName("Zone confidence is penalized when telemetry is STALE")
    void testFuseZoneWithStaleObservation() {
        ZoneDefinitionDto wankhede = zoneRegistry.getZoneById("ZONE_WANKHEDE").orElseThrow();
        NormalizedObservationDto obs = NormalizedObservationDto.builder()
            .id("OBS_STALE_1")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .metricType("CROWD_COUNT")
            .value(2000.0)
            .confidence(0.9)
            .qualityStatus("STALE")
            .freshnessSeconds(300)
            .build();

        ZoneStateDto state = sensorFusionService.fuseZone(wankhede, List.of(obs));
        assertThat(state).isNotNull();
        assertThat(state.getDataQuality()).isEqualTo("STALE");
        assertThat(state.getConfidence()).isLessThan(0.7);
    }
}
