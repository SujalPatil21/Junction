package com.junction.service;

import com.junction.dto.HospitalityDemandSignalDto;
import com.junction.dto.OperationalInterventionDto;
import com.junction.dto.RestaurantDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HospitalityDemandService {

    private final RestaurantService restaurantService;

    public HospitalityDemandService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    public List<HospitalityDemandSignalDto> calculateDemandSignals(int exitOutflowRate) {
        List<HospitalityDemandSignalDto> signals = new ArrayList<>();
        List<RestaurantDto> restaurants = restaurantService.getAllRestaurants(null);

        for (RestaurantDto r : restaurants) {
            int dinersSeeking = (int) Math.round(exitOutflowRate * 0.18);
            if (dinersSeeking > 15) {
                signals.add(HospitalityDemandSignalDto.builder()
                    .targetType("RESTAURANT")
                    .zoneId(r.getZone())
                    .resourceId(r.getId())
                    .incomingCrowdCohortSize(dinersSeeking)
                    .estimatedArrivalTime(Instant.now().plusSeconds(1200).toString())
                    .expectedDurationMinutes(60)
                    .recommendedSurgePricingOrCap(dinersSeeking > r.getAvailableCovers())
                    .build());
            }
        }

        return signals;
    }
}

