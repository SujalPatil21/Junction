package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotPredictionDto {
    private String id;
    private String resourceId;
    private String zoneId;
    private String name;
    private GeoLocationDto location;
    private int currentPressure;
    private int peakPredictedPressure;
    private String severity; // WATCH, HIGH, CRITICAL
    private int projectedOnsetMinutes;
    private int projectedDurationMinutes;
    private int radiusMeters;
    private List<String> keyDrivers;
    private double confidence;
}

