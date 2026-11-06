package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDefinitionDto {
    private String id;
    private String name;
    private String type; // CCTV_CAMERA, TURNSTILE_GATE, WIFI_PROBE, BLE_BEACON, LIDAR, DRONE
    private String zoneId;
    private String resourceId;
    private GeoLocationDto location;
    private List<String> supportedCapabilities;
    private double reliabilityScore;
    private boolean isSimulated;
    private String status; // ONLINE, DEGRADED, OFFLINE
    private String lastSeenAt;
    private Integer latencyMs;
}

