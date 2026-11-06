package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneStateDto {
    private String id;
    private String name;
    private String shortName;
    private int pressure;
    private String pressureLevel; // NORMAL, WATCH, HIGH, CRITICAL
    private String trend;         // INCREASING, STABLE, DECREASING
    private String monitoringStatus; // CONTINUOUS, ADAPTIVE, PERIODIC, ON_DEMAND
    private boolean isEscalated;
    private int currentUtilization;
    private int usableCapacity;
    private int availableCapacity;
    private double spatialDensity;
    private double confidence;
    private int inflowRate;
    private int outflowRate;
    private int netFlow;
    private int predictedPressure15;
    private int predictedPressure30;
    private int predictedPressure60;
    private String dataQuality;
    private List<String> contributingSensors;
    private List<String> missingSensors;
    private List<String> conflicts;
}

