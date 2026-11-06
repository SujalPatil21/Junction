package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalityDemandSignalDto {
    private String targetType; // HOTEL, RESTAURANT
    private String zoneId;
    private String resourceId;
    private int incomingCrowdCohortSize;
    private String estimatedArrivalTime;
    private int expectedDurationMinutes;
    private Boolean recommendedSurgePricingOrCap;
}

