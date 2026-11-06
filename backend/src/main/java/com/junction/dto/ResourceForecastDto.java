package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceForecastDto {
    private String resourceId;
    private String resourceName;
    private String zoneId;
    private int currentPressure;
    private List<ForecastPointDto> forecastPoints;
    private ThresholdCrossingDto thresholdCrossing;
    private List<String> contributingFactors;
    private String algorithmUsed;
    private double confidenceScore;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastPointDto {
        private String label;
        private int minutesFromNow;
        private String projectedTime;
        private int predictedPressure;
        private int confidenceLow;
        private int confidenceHigh;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThresholdCrossingDto {
        private String level;
        private int minutesFromNow;
        private String projectedTime;
    }
}

