package com.junction.service;

import com.junction.dto.GeoLocationDto;
import com.junction.dto.ZoneCoverageProfileDto;
import com.junction.dto.ZoneDefinitionDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ZoneRegistryService {

    private final List<ZoneDefinitionDto> zones = new ArrayList<>();
    private final Map<String, ZoneDefinitionDto> zoneMap = new HashMap<>();
    private final Map<String, ZoneCoverageProfileDto> coverageProfiles = new HashMap<>();

    public ZoneRegistryService() {
        initZones();
        initCoverageProfiles();
    }

    private void initZones() {
        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_WANKHEDE")
            .name("Wankhede Stadium & Perimeter Concourse")
            .shortName("Wankhede Concourse")
            .tier("TIER_1_CRITICAL")
            .defaultMonitoringStatus("CONTINUOUS")
            .center(new GeoLocationDto(18.9389, 72.8258))
            .radiusMeters(350)
            .resourceIds(List.of("WANKHEDE", "WANKHEDE_EXIT"))
            .hotelIds(List.of())
            .connectedRoadIds(List.of("ROAD_MARINE_DR", "ROAD_VEER_NARIMAN", "ROAD_MAHARSHI_KARVE"))
            .nominalPedestrianCapacity(37000)
            .nominalTransitCapacity(0)
            .description("Primary mega-event venue bowl, turnstiles, and perimeter egress concourse.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_CHURCHGATE")
            .name("Churchgate Western Railway Terminal Hub")
            .shortName("Churchgate Station")
            .tier("TIER_1_CRITICAL")
            .defaultMonitoringStatus("CONTINUOUS")
            .center(new GeoLocationDto(18.9355, 72.8272))
            .radiusMeters(280)
            .resourceIds(List.of("CHURCHGATE"))
            .hotelIds(List.of("H2"))
            .connectedRoadIds(List.of("ROAD_VEER_NARIMAN", "ROAD_MAHARSHI_KARVE"))
            .nominalPedestrianCapacity(10000)
            .nominalTransitCapacity(18000)
            .description("Western Railway terminus and primary pedestrian clearance corridor for South Mumbai.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_TAXI_STAGING")
            .name("South Stadium Taxi & Rideshare Staging Zone")
            .shortName("Taxi Staging Bay")
            .tier("TIER_1_CRITICAL")
            .defaultMonitoringStatus("CONTINUOUS")
            .center(new GeoLocationDto(18.9372, 72.8268))
            .radiusMeters(180)
            .resourceIds(List.of("TAXI_ZONE"))
            .hotelIds(List.of())
            .connectedRoadIds(List.of("ROAD_VEER_NARIMAN", "ROAD_MARINE_DR"))
            .nominalPedestrianCapacity(800)
            .nominalTransitCapacity(1200)
            .description("Curbside vehicle pickup bays, taxi queue buffer, and rideshare dispatch staging.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_MARINE_LINES")
            .name("Marine Lines Station & North Concourse")
            .shortName("Marine Lines")
            .tier("TIER_2_SUPPORTING")
            .defaultMonitoringStatus("ADAPTIVE")
            .center(new GeoLocationDto(18.9436, 72.8236))
            .radiusMeters(240)
            .resourceIds(List.of("MARINE_LINES"))
            .hotelIds(List.of())
            .connectedRoadIds(List.of("ROAD_MAHARSHI_KARVE", "ROAD_MARINE_DR"))
            .nominalPedestrianCapacity(8000)
            .nominalTransitCapacity(12000)
            .description("Western Railway intermediate station providing secondary relief for northern egress.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_CSMT")
            .name("CSMT Central & Harbour Terminal Hub")
            .shortName("CSMT Terminal")
            .tier("TIER_2_SUPPORTING")
            .defaultMonitoringStatus("ADAPTIVE")
            .center(new GeoLocationDto(18.9400, 72.8353))
            .radiusMeters(400)
            .resourceIds(List.of("CSMT"))
            .hotelIds(List.of("H3"))
            .connectedRoadIds(List.of("ROAD_DN_ROAD", "ROAD_CENTRAL_SPINE"))
            .nominalPedestrianCapacity(14000)
            .nominalTransitCapacity(25000)
            .description("Central and Harbour Railway terminus hub capturing eastern and suburban passenger dispersal.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_HOTELS_SOUTH")
            .name("Nariman Point & Marine Drive Hospitality Corridor")
            .shortName("South Hospitality")
            .tier("TIER_2_SUPPORTING")
            .defaultMonitoringStatus("PERIODIC")
            .center(new GeoLocationDto(18.9280, 72.8230))
            .radiusMeters(550)
            .resourceIds(List.of())
            .hotelIds(List.of("H1", "H2", "H3"))
            .connectedRoadIds(List.of("ROAD_MARINE_DR"))
            .nominalPedestrianCapacity(5000)
            .nominalTransitCapacity(0)
            .description("Zone A & Zone B accommodation clusters supporting premium event visitors.")
            .build());

        addZone(ZoneDefinitionDto.builder()
            .id("ZONE_DADAR")
            .name("Dadar Artery & Transit Interchange Hub")
            .shortName("Dadar Interchange")
            .tier("TIER_3_PERIPHERAL")
            .defaultMonitoringStatus("ON_DEMAND")
            .center(new GeoLocationDto(19.0183, 72.8434))
            .radiusMeters(600)
            .resourceIds(List.of("DADAR"))
            .hotelIds(List.of("H4"))
            .connectedRoadIds(List.of("ROAD_CENTRAL_SPINE"))
            .nominalPedestrianCapacity(18000)
            .nominalTransitCapacity(35000)
            .description("Dual-railway interchange (Western & Central) and northern bus feeder terminal for crowd redistribution.")
            .build());
    }

    private void addZone(ZoneDefinitionDto z) {
        zones.add(z);
        zoneMap.put(z.getId(), z);
    }

    private void initCoverageProfiles() {
        coverageProfiles.put("ZONE_WANKHEDE", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_WANKHEDE")
            .displayName("Wankhede Stadium & Perimeter Concourse")
            .coverageStatus("FULL")
            .dataSource("SENSOR_FUSED")
            .registeredDeviceCount(6)
            .deviceModalities(List.of("LIDAR", "TURNSTILE_GATE", "CCTV_CAMERA"))
            .maxPermittedConfidence(1.0)
            .fallbackBehavior("NONE")
            .freshnessMaxAgeSeconds(30)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Complete optical LiDAR and turnstile hardware instrumentation on egress gates 1-7.")
            .build());

        coverageProfiles.put("ZONE_CHURCHGATE", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_CHURCHGATE")
            .displayName("Churchgate Western Railway Terminal Hub")
            .coverageStatus("FULL")
            .dataSource("SENSOR_FUSED")
            .registeredDeviceCount(8)
            .deviceModalities(List.of("TURNSTILE_GATE", "CCTV_CAMERA", "WIFI_PROBE", "BLE_BEACON"))
            .maxPermittedConfidence(1.0)
            .fallbackBehavior("NONE")
            .freshnessMaxAgeSeconds(30)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Full turnstile bank, CCTV optical density, and passenger probe tracking on subsurface concourses.")
            .build());

        coverageProfiles.put("ZONE_TAXI_STAGING", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_TAXI_STAGING")
            .displayName("South Stadium Taxi & Rideshare Staging Zone")
            .coverageStatus("FULL")
            .dataSource("SENSOR_FUSED")
            .registeredDeviceCount(4)
            .deviceModalities(List.of("CCTV_CAMERA", "BLE_BEACON", "GPS_FLEET"))
            .maxPermittedConfidence(1.0)
            .fallbackBehavior("NONE")
            .freshnessMaxAgeSeconds(30)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Curbside CCTV queue estimation and beacon array for dispatch flow measurement.")
            .build());

        coverageProfiles.put("ZONE_MARINE_LINES", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_MARINE_LINES")
            .displayName("Marine Lines Station & North Concourse")
            .coverageStatus("PARTIAL")
            .dataSource("SENSOR_FUSED")
            .registeredDeviceCount(2)
            .deviceModalities(List.of("CCTV_CAMERA", "ENTRANCE_COUNTER"))
            .maxPermittedConfidence(0.85)
            .fallbackBehavior("TOPOLOGY_PROPAGATION")
            .freshnessMaxAgeSeconds(45)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Concourse optical monitoring with platform load inferred from corridor flow.")
            .build());

        coverageProfiles.put("ZONE_CSMT", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_CSMT")
            .displayName("CSMT Central & Harbour Terminal Hub")
            .coverageStatus("UNINSTRUMENTED")
            .dataSource("REGIONAL_BASELINE")
            .registeredDeviceCount(0)
            .deviceModalities(List.of())
            .maxPermittedConfidence(0.50)
            .fallbackBehavior("REGIONAL_BASELINE")
            .freshnessMaxAgeSeconds(120)
            .canTriggerOperationalRecommendations(false)
            .assumptions("Regional baseline schedule extrapolation. Direct measurements unavailable without field nodes.")
            .build());

        coverageProfiles.put("ZONE_HOTELS_SOUTH", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_HOTELS_SOUTH")
            .displayName("Nariman Point & Marine Drive Hospitality Corridor")
            .coverageStatus("PARTIAL")
            .dataSource("PARTNER_ESTIMATE")
            .registeredDeviceCount(3)
            .deviceModalities(List.of("HOTEL_INVENTORY", "BLE_BEACON"))
            .maxPermittedConfidence(0.80)
            .fallbackBehavior("PARTNER_ESTIMATE")
            .freshnessMaxAgeSeconds(300)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Partner-reported reservation occupancy and curbside BLE cluster estimation.")
            .build());

        coverageProfiles.put("ZONE_DADAR", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_DADAR")
            .displayName("Dadar Artery & Transit Interchange Hub")
            .coverageStatus("UNINSTRUMENTED")
            .dataSource("REGIONAL_BASELINE")
            .registeredDeviceCount(0)
            .deviceModalities(List.of())
            .maxPermittedConfidence(0.50)
            .fallbackBehavior("REGIONAL_BASELINE")
            .freshnessMaxAgeSeconds(120)
            .canTriggerOperationalRecommendations(false)
            .assumptions("Regional transit baseline extrapolation. Uninstrumented; requires operator manual confirmation for major actions.")
            .build());

        coverageProfiles.put("WANKHEDE_EXIT", ZoneCoverageProfileDto.builder()
            .zoneId("WANKHEDE_EXIT")
            .displayName("Wankhede Concourse Exit Gates Subzone")
            .coverageStatus("FULL")
            .dataSource("SENSOR_FUSED")
            .registeredDeviceCount(3)
            .deviceModalities(List.of("TURNSTILE_GATE", "CCTV_CAMERA"))
            .maxPermittedConfidence(1.0)
            .fallbackBehavior("NONE")
            .freshnessMaxAgeSeconds(30)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Egress throughput concourse monitoring at stadium gates 1–7 with flow rate metering.")
            .build());

        coverageProfiles.put("ZONE_MAHAPALIKA", ZoneCoverageProfileDto.builder()
            .zoneId("ZONE_MAHAPALIKA")
            .displayName("Mahapalika Marg Evacuation & Transfer Corridor")
            .coverageStatus("PARTIAL")
            .dataSource("TOPOLOGY_PROPAGATION")
            .registeredDeviceCount(1)
            .deviceModalities(List.of("CCTV_CAMERA"))
            .maxPermittedConfidence(0.70)
            .fallbackBehavior("TOPOLOGY_PROPAGATION")
            .freshnessMaxAgeSeconds(60)
            .canTriggerOperationalRecommendations(true)
            .assumptions("Pedestrian arterial corridor between Churchgate and CSMT concourses.")
            .build());
    }

    public List<ZoneDefinitionDto> getAllZones() {
        return Collections.unmodifiableList(zones);
    }

    public Optional<ZoneDefinitionDto> getZoneById(String id) {
        return Optional.ofNullable(zoneMap.get(id));
    }

    public Map<String, ZoneCoverageProfileDto> getAllCoverageProfiles() {
        return Collections.unmodifiableMap(coverageProfiles);
    }

    public Optional<ZoneCoverageProfileDto> getCoverageProfile(String zoneId) {
        return Optional.ofNullable(coverageProfiles.get(zoneId));
    }
}

