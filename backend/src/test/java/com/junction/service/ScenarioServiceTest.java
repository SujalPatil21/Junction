package com.junction.service;

import com.junction.dto.ScenarioDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioServiceTest {

    private ScenarioService scenarioService;

    @BeforeEach
    void setUp() {
        scenarioService = new ScenarioService();
    }

    @Test
    @DisplayName("Default active scenario is NORMAL")
    void testDefaultScenarioIsNormal() {
        assertThat(scenarioService.getActiveScenarioId()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("Setting active scenario transitions state correctly")
    void testSetActiveScenario() {
        scenarioService.setActiveScenarioId("POST_EVENT_SURGE");
        assertThat(scenarioService.getActiveScenarioId()).isEqualTo("POST_EVENT_SURGE");
        ScenarioDto active = scenarioService.getActiveScenario();
        assertThat(active.getLabel()).isEqualTo("Post-Event Surge");
    }

    @Test
    @DisplayName("All 4 standard scenarios are initialized")
    void testGetAllScenarios() {
        List<ScenarioDto> all = scenarioService.getAllScenarios();
        assertThat(all).hasSize(4);
        assertThat(all).extracting(ScenarioDto::getId)
            .containsExactlyInAnyOrder("NORMAL", "POST_EVENT_SURGE", "TRANSPORT_DISRUPTION", "HEAVY_RAIN");
    }
}
