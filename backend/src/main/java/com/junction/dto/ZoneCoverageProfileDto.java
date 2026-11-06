package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneCoverageProfileDto {
    private String zoneId;
    private String displayName;
    private String coverageStatus; // FULL, PARTIAL, UNINSTRUMENTED
    private String dataSource;     // SENSOR_FUSED, REGIONAL_BASELINE, PARTNER_ESTIMATE, TOPOLOGY_PROPAGATION
    private int registeredDeviceCount;
    private List<String> deviceModalities;
    private double maxPermittedConfidence;
    private String fallbackBehavior;
    private int freshnessMaxAgeSeconds;
    private boolean canTriggerOperationalRecommendations;
    private String assumptions;
}

