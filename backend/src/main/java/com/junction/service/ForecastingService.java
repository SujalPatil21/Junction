package com.junction.service;

import com.junction.dto.ResourceForecastDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastingService {

    public ResourceForecastDto generateForecast(
        String resourceId,
        String resourceName,
        String zoneId,
        int currentPressure,
        String scenarioId,
        double baseConfidence
    ) {
        int[] horizons = new int[]{15, 30, 45, 60};
        long nowMillis = System.currentTimeMillis();
        List<ResourceForecastDto.ForecastPointDto> points = new ArrayList<>();
        List<String> contributingFactors = new ArrayList<>();

        double surgeFactor;
        if ("POST_EVENT_SURGE".equalsIgnoreCase(scenarioId)) {
            surgeFactor = 0.35;
            contributingFactors.add("Post-event stadium egress curve (+35% pressure gradient)");
        } else if ("TRANSPORT_DISRUPTION".equalsIgnoreCase(scenarioId)) {
            surgeFactor = 0.45;
            contributingFactors.add("Transit platform throttling backlog accumulation");
        } else if ("HEAVY_RAIN".equalsIgnoreCase(scenarioId)) {
            surgeFactor = 0.25;
            contributingFactors.add("Weather-induced pedestrian sheltering and surface road delays");
        } else {
            surgeFactor = -0.05;
            contributingFactors.add("Nominal background dispersal rate");
        }

        ResourceForecastDto.ThresholdCrossingDto thresholdCrossing = null;

        for (int mins : horizons) {
            long projMillis = nowMillis + mins * 60_000L;
            String projIso = Instant.ofEpochMilli(projMillis).toString();

            int projectedPressure = Math.min(100, Math.max(0, (int) Math.round(currentPressure + (surgeFactor * mins))));
            double uncertainty = (mins / 15.0) * 3.5;
            int confLow = Math.max(0, (int) Math.round(projectedPressure - uncertainty));
            int confHigh = Math.min(100, (int) Math.round(projectedPressure + uncertainty));

            String lvl = getPressureLevel(projectedPressure);
            if (thresholdCrossing == null && ("HIGH".equals(lvl) || "CRITICAL".equals(lvl)) && currentPressure < 85) {
                thresholdCrossing = ResourceForecastDto.ThresholdCrossingDto.builder()
                    .level(lvl)
                    .minutesFromNow(mins)
                    .projectedTime(projIso)
                    .build();
            }

            points.add(ResourceForecastDto.ForecastPointDto.builder()
                .label("+" + mins + "m")
                .minutesFromNow(mins)
                .projectedTime(projIso)
                .predictedPressure(projectedPressure)
                .confidenceLow(confLow)
                .confidenceHigh(confHigh)
                .build());
        }

        return ResourceForecastDto.builder()
            .resourceId(resourceId)
            .resourceName(resourceName)
            .zoneId(zoneId)
            .currentPressure(currentPressure)
            .forecastPoints(points)
            .thresholdCrossing(thresholdCrossing)
            .contributingFactors(contributingFactors)
            .algorithmUsed("SCENARIO_CURVE")
            .confidenceScore(Math.max(0.6, baseConfidence - 0.1))
            .build();
    }

    private String getPressureLevel(int pressure) {
        if (pressure >= 90) return "CRITICAL";
        if (pressure >= 80) return "HIGH";
        if (pressure >= 65) return "WATCH";
        return "NORMAL";
    }
}

