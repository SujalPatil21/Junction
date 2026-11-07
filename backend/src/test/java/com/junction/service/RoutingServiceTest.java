package com.junction.service;

import com.junction.dto.GeoLocationDto;
import com.junction.dto.RouteResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingServiceTest {

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoutingService();
    }

    @Test
    @DisplayName("Calculates valid route between Wankhede and Churchgate")
    void testCalculateValidRoute() {
        GeoLocationDto start = new GeoLocationDto(18.9389, 72.8258); // Wankhede
        GeoLocationDto end = new GeoLocationDto(18.9355, 72.8272);   // Churchgate

        RouteResultDto result = routingService.calculateRoute(start, end);
        assertThat(result).isNotNull();
        assertThat(result.isValidRoute()).isTrue();
        assertThat(result.getDistanceKm()).isGreaterThan(0);
        assertThat(result.getEstimatedTimeMin()).isGreaterThan(0);
        assertThat(result.getPath()).isNotEmpty();
    }

    @Test
    @DisplayName("Returns invalid route when start or end is null")
    void testNullLocationsReturnInvalid() {
        RouteResultDto result = routingService.calculateRoute(null, new GeoLocationDto(18.9, 72.8));
        assertThat(result.isValidRoute()).isFalse();
        assertThat(result.getWarning()).contains("null");
    }
}
