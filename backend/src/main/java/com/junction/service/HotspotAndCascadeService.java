package com.junction.service;

import com.junction.dto.CascadeAnalysisDto;
import com.junction.dto.GeoLocationDto;
import com.junction.dto.HotspotPredictionDto;
import com.junction.dto.ZoneDefinitionDto;
import com.junction.dto.ZoneStateDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HotspotAndCascadeService {

    private final ZoneRegistryService zoneRegistry;

    public HotspotAndCascadeService(ZoneRegistryService zoneRegistry) {
        this.zoneRegistry = zoneRegistry;
    }

    public List<HotspotPredictionDto> detectHotspots(List<ZoneStateDto> zones) {
        List<HotspotPredictionDto> hotspots = new ArrayList<>();

        for (ZoneStateDto zone : zones) {
            if (zone.getPressure() >= 75 || "HIGH".equals(zone.getPressureLevel()) || "CRITICAL".equals(zone.getPressureLevel())) {
                List<String> drivers = new ArrayList<>();
                if (zone.getPressure() >= 85) drivers.add("Severe capacity saturation exceeding 85%");
                if ("INCREASING".equals(zone.getTrend())) drivers.add("Rapid inflow rate accelerating crowd accumulation");
                if (zone.getAvailableCapacity() < zone.getUsableCapacity() * 0.15) drivers.add("Available safety capacity below 15%");

                Optional<ZoneDefinitionDto> defOpt = zoneRegistry.getZoneById(zone.getId());
                GeoLocationDto location = defOpt.map(ZoneDefinitionDto::getCenter).orElse(new GeoLocationDto(18.9389, 72.8258));
                int radiusMeters = defOpt.map(ZoneDefinitionDto::getRadiusMeters).orElse(250);

                hotspots.add(HotspotPredictionDto.builder()
                    .id("HOTSPOT_" + zone.getId())
                    .resourceId(zone.getId())
                    .zoneId(zone.getId())
                    .name(zone.getName())
                    .location(location)
                    .currentPressure(zone.getPressure())
                    .peakPredictedPressure(zone.getPredictedPressure30())
                    .severity(zone.getPressure() >= 90 ? "CRITICAL" : (zone.getPressure() >= 80 ? "HIGH" : "WATCH"))
                    .projectedOnsetMinutes(10)
                    .projectedDurationMinutes(45)
                    .radiusMeters(radiusMeters)
                    .keyDrivers(drivers)
                    .confidence(zone.getConfidence())
                    .build());
            }
        }

        return hotspots;
    }

    public CascadeAnalysisDto analyzeCascade(String originZoneId, int originPressure) {
        String timestamp = Instant.now().toString();

        List<CascadeAnalysisDto.CascadeNodeProjectionDto> pathway = List.of(
            CascadeAnalysisDto.CascadeNodeProjectionDto.builder()
                .nodeId("CASCADE_ORIGIN")
                .resourceId("WANKHEDE_EXIT")
                .label("Wankhede Gates & Concourse Egress")
                .currentPressure(originPressure)
                .projectedPressure(originPressure)
                .status(originPressure >= 90 ? "CRITICAL" : (originPressure >= 80 ? "HIGH" : "NORMAL"))
                .leadTimeMinutes(0)
                .influenceWeight(1.0)
                .mitigationAvailable(true)
                .build(),

            CascadeAnalysisDto.CascadeNodeProjectionDto.builder()
                .nodeId("CASCADE_CORRIDOR")
                .resourceId("ROAD_MARINE_DR")
                .label("Marine Drive Pedestrian Corridor")
                .currentPressure((int) Math.round(originPressure * 0.88))
                .projectedPressure(Math.min(100, (int) Math.round(originPressure * 0.95)))
                .status((originPressure * 0.88) >= 80 ? "HIGH" : "NORMAL")
                .leadTimeMinutes(12)
                .influenceWeight(0.85)
                .mitigationAvailable(true)
                .build(),

            CascadeAnalysisDto.CascadeNodeProjectionDto.builder()
                .nodeId("CASCADE_TRANSIT")
                .resourceId("CHURCHGATE")
                .label("Churchgate Western Railway Terminal")
                .currentPressure((int) Math.round(originPressure * 0.82))
                .projectedPressure(Math.min(100, (int) Math.round(originPressure * 0.98)))
                .status((originPressure * 0.82) >= 80 ? "HIGH" : "NORMAL")
                .leadTimeMinutes(20)
                .influenceWeight(0.90)
                .mitigationAvailable(true)
                .build(),

            CascadeAnalysisDto.CascadeNodeProjectionDto.builder()
                .nodeId("CASCADE_PICKUP")
                .resourceId("TAXI_ZONE")
                .label("South Taxi & Rideshare Staging Bay")
                .currentPressure((int) Math.round(originPressure * 0.75))
                .projectedPressure(Math.min(100, (int) Math.round(originPressure * 0.92)))
                .status((originPressure * 0.75) >= 80 ? "HIGH" : "NORMAL")
                .leadTimeMinutes(25)
                .influenceWeight(0.70)
                .mitigationAvailable(true)
                .build(),

            CascadeAnalysisDto.CascadeNodeProjectionDto.builder()
                .nodeId("CASCADE_HOSPITALITY")
                .resourceId("HOSPITALITY_SOUTH")
                .label("Marine Drive Dining & Hotel Corridor")
                .currentPressure((int) Math.round(originPressure * 0.65))
                .projectedPressure(Math.min(100, (int) Math.round(originPressure * 0.85)))
                .status((originPressure * 0.65) >= 80 ? "HIGH" : "NORMAL")
                .leadTimeMinutes(40)
                .influenceWeight(0.60)
                .mitigationAvailable(true)
                .build()
        );

        return CascadeAnalysisDto.builder()
            .rootIncidentId("INCIDENT_" + originZoneId + "_" + System.currentTimeMillis())
            .rootResourceId(originZoneId)
            .timestamp(timestamp)
            .affectedPathways(List.of(pathway))
            .summary("Surge from " + originZoneId + " is propagating outward across transit hubs and hospitality within a 40-minute window.")
            .confidence(0.88)
            .build();
    }
}

