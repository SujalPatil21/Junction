package com.junction.service;

import com.junction.dto.DeviceDefinitionDto;
import com.junction.dto.GeoLocationDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DeviceRegistryService {

    private final Map<String, DeviceDefinitionDto> devices = new ConcurrentHashMap<>();

    public DeviceRegistryService() {
        initDefaultDevices();
    }

    private void initDefaultDevices() {
        register(DeviceDefinitionDto.builder()
            .id("DEV_CCTV_WANKHEDE_01")
            .name("Wankhede Gate 1 Primary Egress Camera")
            .type("CCTV_CAMERA")
            .zoneId("ZONE_WANKHEDE")
            .resourceId("WANKHEDE_EXIT")
            .location(new GeoLocationDto(18.9389, 72.8258))
            .supportedCapabilities(List.of("CROWD_COUNT", "DENSITY", "FLOW_RATE"))
            .reliabilityScore(0.92)
            .isSimulated(false)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(45)
            .build());

        register(DeviceDefinitionDto.builder()
            .id("DEV_CCTV_WANKHEDE_02")
            .name("Wankhede North Concourse Overhead Optical")
            .type("CCTV_CAMERA")
            .zoneId("ZONE_WANKHEDE")
            .resourceId("WANKHEDE_EXIT")
            .location(new GeoLocationDto(18.9395, 72.8260))
            .supportedCapabilities(List.of("CROWD_COUNT", "DENSITY"))
            .reliabilityScore(0.89)
            .isSimulated(true)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(60)
            .build());

        register(DeviceDefinitionDto.builder()
            .id("DEV_TURNSTILE_WANKHEDE_01")
            .name("Wankhede Gate 1-3 Turnstile Array")
            .type("TURNSTILE_GATE")
            .zoneId("ZONE_WANKHEDE")
            .resourceId("WANKHEDE_EXIT")
            .location(new GeoLocationDto(18.9388, 72.8257))
            .supportedCapabilities(List.of("OUTFLOW_RATE", "CROWD_COUNT"))
            .reliabilityScore(0.98)
            .isSimulated(true)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(12)
            .build());

        register(DeviceDefinitionDto.builder()
            .id("DEV_TURNSTILE_CHURCHGATE_01")
            .name("Churchgate Subsurface Turnstile Gatebank")
            .type("TURNSTILE_GATE")
            .zoneId("ZONE_CHURCHGATE")
            .resourceId("CHURCHGATE")
            .location(new GeoLocationDto(18.9355, 72.8272))
            .supportedCapabilities(List.of("INFLOW_RATE", "CROWD_COUNT"))
            .reliabilityScore(0.97)
            .isSimulated(true)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(15)
            .build());

        register(DeviceDefinitionDto.builder()
            .id("DEV_CCTV_CHURCHGATE_01")
            .name("Churchgate Main Concourse Optical Counter")
            .type("CCTV_CAMERA")
            .zoneId("ZONE_CHURCHGATE")
            .resourceId("CHURCHGATE")
            .location(new GeoLocationDto(18.9356, 72.8271))
            .supportedCapabilities(List.of("CROWD_COUNT", "DENSITY", "QUEUE_LENGTH"))
            .reliabilityScore(0.91)
            .isSimulated(true)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(50)
            .build());

        register(DeviceDefinitionDto.builder()
            .id("DEV_CCTV_TAXI_01")
            .name("Marine Drive Taxi Bay Curbside Optical")
            .type("CCTV_CAMERA")
            .zoneId("ZONE_TAXI_STAGING")
            .resourceId("TAXI_ZONE")
            .location(new GeoLocationDto(18.9372, 72.8268))
            .supportedCapabilities(List.of("QUEUE_LENGTH", "CROWD_COUNT"))
            .reliabilityScore(0.88)
            .isSimulated(true)
            .status("ONLINE")
            .lastSeenAt(Instant.now().toString())
            .latencyMs(65)
            .build());
    }

    public void register(DeviceDefinitionDto device) {
        if (device == null || device.getId() == null || device.getId().isBlank()) {
            throw new IllegalArgumentException("Device id must not be null or empty");
        }
        devices.put(device.getId(), device);
    }

    public Optional<DeviceDefinitionDto> get(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

    public List<DeviceDefinitionDto> getAll() {
        return new ArrayList<>(devices.values());
    }

    public List<DeviceDefinitionDto> getByZone(String zoneId) {
        return devices.values().stream()
            .filter(d -> Objects.equals(d.getZoneId(), zoneId))
            .collect(Collectors.toList());
    }

    public List<DeviceDefinitionDto> getByType(String type) {
        return devices.values().stream()
            .filter(d -> Objects.equals(d.getType(), type))
            .collect(Collectors.toList());
    }

    public int count() {
        return devices.size();
    }
}

