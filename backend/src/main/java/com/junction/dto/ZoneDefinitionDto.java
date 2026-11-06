package com.junction.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDefinitionDto {
    private String id;
    private String name;
    private String shortName;
    private String tier;
    private String defaultMonitoringStatus;
    private GeoLocationDto center;
    private int radiusMeters;
    private List<String> resourceIds;
    private List<String> hotelIds;
    private List<String> connectedRoadIds;
    private int nominalPedestrianCapacity;
    private int nominalTransitCapacity;
    private String description;
}

