package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResultDto {
    private double distanceKm;
    private double estimatedTimeMin;
    private List<GeoLocationDto> path;
    private List<String> roadNames;
    private boolean isValidRoute;
    private double congestionFactor;
    private String warning;
}

