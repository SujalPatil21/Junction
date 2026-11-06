package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalizedObservationDto {
    private String id;
    private String sourceId;
    private String zoneId;
    private String resourceId;
    private String metricType; // CROWD_COUNT, DENSITY, QUEUE_LENGTH, INFLOW_RATE, OUTFLOW_RATE
    private Double value;
    private String unit;
    private Double confidence;
    private String qualityStatus; // FRESH, STALE, DELAYED, CONFLICTING, INVALID
    private Integer freshnessSeconds;
    private String observedAt;
    private String receivedAt;
}

