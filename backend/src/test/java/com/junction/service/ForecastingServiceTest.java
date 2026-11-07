package com.junction.service;

import com.junction.dto.ResourceForecastDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastingServiceTest {

    private ForecastingService forecastingService;

    @BeforeEach
    void setUp() {
        forecastingService = new ForecastingService();
    }

    @Test
    @DisplayName("Generate forecast computes 15, 30, 45, and 60-minute prediction horizons")
    void testForecastHorizons() {
        ResourceForecastDto forecast = forecastingService.generateForecast(
            "WANKHEDE_EXIT",
            "Wankhede Exit Gates",
            "ZONE_WANKHEDE",
            70,
            "POST_EVENT_SURGE",
            0.92
        );

        assertThat(forecast).isNotNull();
        assertThat(forecast.getResourceId()).isEqualTo("WANKHEDE_EXIT");
        assertThat(forecast.getForecastPoints()).hasSize(4);
        assertThat(forecast.getForecastPoints()).extracting(ResourceForecastDto.ForecastPointDto::getMinutesFromNow)
            .containsExactly(15, 30, 45, 60);
        assertThat(forecast.getContributingFactors()).anyMatch(f -> f.contains("Post-event stadium egress"));
    }

    @Test
    @DisplayName("Transport disruption accelerates pressure gradient and triggers threshold crossing")
    void testDisruptionThresholdCrossing() {
        ResourceForecastDto forecast = forecastingService.generateForecast(
            "CHURCHGATE",
            "Churchgate Station",
            "ZONE_CHURCHGATE",
            82,
            "TRANSPORT_DISRUPTION",
            0.88
        );

        assertThat(forecast).isNotNull();
        assertThat(forecast.getThresholdCrossing()).isNotNull();
        assertThat(forecast.getThresholdCrossing().getLevel()).isIn("HIGH", "CRITICAL");
    }
}
