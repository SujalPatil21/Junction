package com.junction.service;

import com.junction.dto.DeviceDefinitionDto;
import com.junction.dto.NormalizedObservationDto;
import com.junction.dto.ZoneDefinitionDto;
import com.junction.dto.ZoneStateDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensorFusionService {

    private final DeviceRegistryService deviceRegistry;
    private final ZoneRegistryService zoneRegistry;

    // Operational capacity profiles matching TypeScript ZONE_CAPACITY_PROFILES
    private static final Map<String, Integer> OPERATIONAL_CAPACITIES = Map.of(
        "ZONE_WANKHEDE", 4000,
        "ZONE_CHURCHGATE", 10000,
        "ZONE_TAXI_STAGING", 800,
        "ZONE_MARINE_LINES", 8000,
        "ZONE_CSMT", 14000,
        "ZONE_HOTELS_SOUTH", 3000,
        "ZONE_DADAR", 18000
    );

    public SensorFusionService(DeviceRegistryService deviceRegistry, ZoneRegistryService zoneRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.zoneRegistry = zoneRegistry;
    }

    public ZoneStateDto fuseZone(ZoneDefinitionDto zone, List<NormalizedObservationDto> observations) {
        List<NormalizedObservationDto> zoneObs = observations.stream()
            .filter(o -> Objects.equals(o.getZoneId(), zone.getId()))
            .filter(o -> !"INVALID".equals(o.getQualityStatus()))
            .collect(Collectors.toList());

        List<DeviceDefinitionDto> expectedDevices = deviceRegistry.getByZone(zone.getId());
        Set<String> observedDeviceIds = zoneObs.stream().map(NormalizedObservationDto::getSourceId).collect(Collectors.toSet());
        List<String> missingSensors = expectedDevices.stream()
            .filter(d -> !observedDeviceIds.contains(d.getId()))
            .map(d -> d.getName() + " (" + d.getId() + ")")
            .collect(Collectors.toList());

        int usableCapacity = OPERATIONAL_CAPACITIES.getOrDefault(zone.getId(), zone.getNominalPedestrianCapacity());
        int safetyUsableCapacity = (int) Math.round(usableCapacity * 0.93);

        if (zoneObs.isEmpty()) {
            // Simulated baseline fallback
            int defaultPressure = switch (zone.getId()) {
                case "ZONE_WANKHEDE" -> 72;
                case "ZONE_CHURCHGATE" -> 74;
                case "ZONE_TAXI_STAGING" -> 45;
                case "ZONE_MARINE_LINES" -> 44;
                case "ZONE_CSMT" -> 58;
                case "ZONE_HOTELS_SOUTH" -> 52;
                case "ZONE_DADAR" -> 58;
                default -> 50;
            };

            int util = (int) Math.round(safetyUsableCapacity * (defaultPressure / 100.0));
            double conf = "TIER_1_CRITICAL".equals(zone.getTier()) ? 0.65 : 0.50;

            return ZoneStateDto.builder()
                .id(zone.getId())
                .name(zone.getName())
                .shortName(zone.getShortName())
                .pressure(defaultPressure)
                .pressureLevel(calculatePressureLevel(defaultPressure))
                .trend("STABLE")
                .monitoringStatus(zone.getDefaultMonitoringStatus())
                .isEscalated(false)
                .currentUtilization(util)
                .usableCapacity(usableCapacity)
                .availableCapacity(Math.max(0, usableCapacity - util))
                .spatialDensity(Math.round((util / 2500.0) * 100.0) / 100.0)
                .confidence(conf)
                .inflowRate((int) Math.round(util * 0.08))
                .outflowRate((int) Math.round(util * 0.06))
                .netFlow((int) Math.round(util * 0.02))
                .predictedPressure15(Math.min(99, defaultPressure + 5))
                .predictedPressure30(Math.min(99, defaultPressure + 10))
                .predictedPressure60(Math.min(99, defaultPressure + 15))
                .dataQuality(expectedDevices.isEmpty() ? "INTERPOLATED" : "STALE")
                .contributingSensors(List.of())
                .missingSensors(missingSensors)
                .conflicts(List.of())
                .build();
        }

        // Weighted sensor fusion
        double weightedCountSum = 0;
        double totalWeight = 0;
        List<String> contributingSensors = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        boolean hasConflict = false;

        for (NormalizedObservationDto obs : zoneObs) {
            Optional<DeviceDefinitionDto> devOpt = deviceRegistry.get(obs.getSourceId());
            double reliability = devOpt.map(DeviceDefinitionDto::getReliabilityScore).orElse(0.85);
            String deviceName = devOpt.map(DeviceDefinitionDto::getName).orElse(obs.getSourceId());

            double freshnessWeight = 1.0;
            if ("STALE".equals(obs.getQualityStatus()) || (obs.getFreshnessSeconds() != null && obs.getFreshnessSeconds() > 120)) {
                freshnessWeight = 0.35;
            } else if ("DELAYED".equals(obs.getQualityStatus())) {
                freshnessWeight = 0.75;
            }

            if ("CONFLICTING".equals(obs.getQualityStatus())) {
                hasConflict = true;
                conflicts.add("Observation conflict from " + deviceName + ": reading deviated significantly from trend.");
            }

            double w = obs.getConfidence() * reliability * freshnessWeight;
            contributingSensors.add(deviceName + " (" + obs.getSourceId() + "): " + obs.getValue() + " " + (obs.getUnit() != null ? obs.getUnit() : ""));

            if ("CROWD_COUNT".equals(obs.getMetricType()) || obs.getMetricType() == null) {
                weightedCountSum += obs.getValue() * w;
                totalWeight += w;
            }
        }

        int fusedOccupancy = totalWeight > 0 ? (int) Math.round(weightedCountSum / totalWeight) : 0;
        int basePressure = Math.min(100, (int) Math.round(((double) fusedOccupancy / safetyUsableCapacity) * 100));
        int conflictPenalty = hasConflict ? 10 : 0;
        int totalPressure = Math.min(99, Math.max(5, basePressure + conflictPenalty));

        double fusedConfidence = totalWeight > 0 ? Math.min(0.98, Math.max(0.35, totalWeight / zoneObs.size())) : 0.5;
        if (hasConflict) fusedConfidence *= 0.7;
        fusedConfidence = Math.round(fusedConfidence * 100.0) / 100.0;

        int inflow = (int) Math.round(fusedOccupancy * 0.08);
        int outflow = (int) Math.round(fusedOccupancy * 0.06);

        return ZoneStateDto.builder()
            .id(zone.getId())
            .name(zone.getName())
            .shortName(zone.getShortName())
            .pressure(totalPressure)
            .pressureLevel(calculatePressureLevel(totalPressure))
            .trend(inflow > outflow ? "INCREASING" : (outflow > inflow ? "DECREASING" : "STABLE"))
            .monitoringStatus(totalPressure >= 85 ? "CONTINUOUS" : zone.getDefaultMonitoringStatus())
            .isEscalated(totalPressure >= 85)
            .currentUtilization(fusedOccupancy)
            .usableCapacity(usableCapacity)
            .availableCapacity(Math.max(0, usableCapacity - fusedOccupancy))
            .spatialDensity(Math.round((fusedOccupancy / 2500.0) * 100.0) / 100.0)
            .confidence(fusedConfidence)
            .inflowRate(inflow)
            .outflowRate(outflow)
            .netFlow(inflow - outflow)
            .predictedPressure15(Math.min(99, totalPressure + 5))
            .predictedPressure30(Math.min(99, totalPressure + 10))
            .predictedPressure60(Math.min(99, totalPressure + 14))
            .dataQuality(hasConflict ? "CONFLICTING" : "FRESH")
            .contributingSensors(contributingSensors)
            .missingSensors(missingSensors)
            .conflicts(conflicts)
            .build();
    }

    public List<ZoneStateDto> fuseAllZones(List<NormalizedObservationDto> observations) {
        return zoneRegistry.getAllZones().stream()
            .map(z -> fuseZone(z, observations))
            .collect(Collectors.toList());
    }

    private String calculatePressureLevel(int pressure) {
        if (pressure >= 90) return "CRITICAL";
        if (pressure >= 80) return "HIGH";
        if (pressure >= 65) return "WATCH";
        return "NORMAL";
    }
}

