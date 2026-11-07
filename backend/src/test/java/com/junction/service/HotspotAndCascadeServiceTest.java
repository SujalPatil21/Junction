package com.junction.service;

import com.junction.dto.CascadeAnalysisDto;
import com.junction.dto.HotspotPredictionDto;
import com.junction.dto.ZoneStateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotspotAndCascadeServiceTest {

    private HotspotAndCascadeService hotspotService;

    @BeforeEach
    void setUp() {
        ZoneRegistryService zoneRegistry = new ZoneRegistryService();
        hotspotService = new HotspotAndCascadeService(zoneRegistry);
    }

    @Test
    @DisplayName("Detects hotspots for zones with pressure >= 75")
    void testDetectHotspots() {
        ZoneStateDto highPressureZone = ZoneStateDto.builder()
            .id("ZONE_WANKHEDE")
            .name("Wankhede Stadium & Perimeter Concourse")
            .pressure(88)
            .predictedPressure30(94)
            .pressureLevel("HIGH")
            .confidence(0.95)
            .availableCapacity(500)
            .usableCapacity(4000)
            .trend("INCREASING")
            .build();

        ZoneStateDto normalZone = ZoneStateDto.builder()
            .id("ZONE_DADAR")
            .name("Dadar Hub")
            .pressure(40)
            .pressureLevel("NORMAL")
            .confidence(0.5)
            .build();

        List<HotspotPredictionDto> hotspots = hotspotService.detectHotspots(List.of(highPressureZone, normalZone));
        assertThat(hotspots).hasSize(1);
        assertThat(hotspots.get(0).getZoneId()).isEqualTo("ZONE_WANKHEDE");
        assertThat(hotspots.get(0).getSeverity()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Analyze cascade propagates pressure through connected egress nodes")
    void testAnalyzeCascade() {
        CascadeAnalysisDto cascade = hotspotService.analyzeCascade("ZONE_WANKHEDE", 85);
        assertThat(cascade).isNotNull();
        assertThat(cascade.getRootResourceId()).isEqualTo("ZONE_WANKHEDE");
        assertThat(cascade.getAffectedPathways()).isNotEmpty();
    }
}
