package com.junction.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioDto {
    private String id;
    private String label;
    private String description;
    private Map<String, ScenarioPressureDataDto> pressure;
    private Map<String, String> transportStatus;
    private String weatherCondition;
    private double hotelPressureMultiplier;
    private double restaurantPressureMultiplier;
    private double travelTimeMultiplier;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScenarioPressureDataDto {
        private int pressure;
        private int predictedPressure15;
        private int predictedPressure30;
        private int predictedPressure60;
    }
}

