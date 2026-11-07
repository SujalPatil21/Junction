package com.junction.service;

import com.junction.dto.HospitalityDemandSignalDto;
import com.junction.dto.RestaurantDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospitalityDemandServiceTest {

    @Mock
    private RestaurantService restaurantService;

    private HospitalityDemandService hospitalityService;

    @BeforeEach
    void setUp() {
        hospitalityService = new HospitalityDemandService(restaurantService);
    }

    @Test
    @DisplayName("Demand propagation calculates signals when exit outflow is substantial")
    void testCalculateDemandSignals() {
        RestaurantDto r1 = RestaurantDto.builder().id("R1").name("Trishna").zone("ZONE_A").availableCovers(20).build();
        when(restaurantService.getAllRestaurants(null)).thenReturn(List.of(r1));

        List<HospitalityDemandSignalDto> signals = hospitalityService.calculateDemandSignals(500);
        assertThat(signals).isNotEmpty();
        assertThat(signals.get(0).getResourceId()).isEqualTo("R1");
        assertThat(signals.get(0).getIncomingCrowdCohortSize()).isEqualTo(90); // 500 * 0.18 = 90
        assertThat(signals.get(0).getRecommendedSurgePricingOrCap()).isTrue(); // 90 > 20
    }

    @Test
    @DisplayName("Negligible outflow produces no demand signals")
    void testNegligibleOutflow() {
        RestaurantDto r1 = RestaurantDto.builder().id("R1").name("Trishna").zone("ZONE_A").availableCovers(20).build();
        when(restaurantService.getAllRestaurants(null)).thenReturn(List.of(r1));

        List<HospitalityDemandSignalDto> signals = hospitalityService.calculateDemandSignals(50); // 50 * 0.18 = 9 <= 15
        assertThat(signals).isEmpty();
    }
}
