package com.junction.service;

import com.junction.dto.ScenarioDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScenarioService {

    private final Map<String, ScenarioDto> scenarios = new ConcurrentHashMap<>();
    private volatile String activeScenarioId = "NORMAL";

    public ScenarioService() {
        initScenarios();
    }

    private void initScenarios() {
        scenarios.put("NORMAL", ScenarioDto.builder()
            .id("NORMAL")
            .label("Normal")
            .description("Baseline event conditions — steady inflow, manageable pressure.")
            .weatherCondition("NORMAL")
            .hotelPressureMultiplier(1.0)
            .restaurantPressureMultiplier(1.0)
            .travelTimeMultiplier(1.0)
            .transportStatus(Map.of("CHURCHGATE", "OPERATIONAL", "CSMT", "OPERATIONAL", "DADAR", "OPERATIONAL"))
            .build());

        scenarios.put("POST_EVENT_SURGE", ScenarioDto.builder()
            .id("POST_EVENT_SURGE")
            .label("Post-Event Surge")
            .description("Match ended — 33,000 attendees exiting simultaneously.")
            .weatherCondition("NORMAL")
            .hotelPressureMultiplier(1.2)
            .restaurantPressureMultiplier(1.4)
            .travelTimeMultiplier(1.5)
            .transportStatus(Map.of("CHURCHGATE", "REDUCED", "CSMT", "OPERATIONAL", "DADAR", "OPERATIONAL"))
            .build());

        scenarios.put("TRANSPORT_DISRUPTION", ScenarioDto.builder()
            .id("TRANSPORT_DISRUPTION")
            .label("Transport Disruption")
            .description("Western Railway signal fault — services delayed 20 min.")
            .weatherCondition("NORMAL")
            .hotelPressureMultiplier(1.1)
            .restaurantPressureMultiplier(1.2)
            .travelTimeMultiplier(2.0)
            .transportStatus(Map.of("CHURCHGATE", "DISRUPTED", "CSMT", "OPERATIONAL", "DADAR", "OPERATIONAL"))
            .build());

        scenarios.put("HEAVY_RAIN", ScenarioDto.builder()
            .id("HEAVY_RAIN")
            .label("Heavy Rain / Monsoon")
            .description("Monsoon downpour — waterlogging on low-lying egress routes.")
            .weatherCondition("RAIN")
            .hotelPressureMultiplier(1.3)
            .restaurantPressureMultiplier(1.1)
            .travelTimeMultiplier(1.8)
            .transportStatus(Map.of("CHURCHGATE", "REDUCED", "CSMT", "REDUCED", "DADAR", "OPERATIONAL"))
            .build());
    }

    public List<ScenarioDto> getAllScenarios() {
        return new ArrayList<>(scenarios.values());
    }

    public Optional<ScenarioDto> getScenario(String id) {
        return Optional.ofNullable(scenarios.get(id));
    }

    public String getActiveScenarioId() {
        return activeScenarioId;
    }

    public ScenarioDto getActiveScenario() {
        return scenarios.get(activeScenarioId);
    }

    public void setActiveScenarioId(String id) {
        if (!scenarios.containsKey(id)) {
            throw new IllegalArgumentException("Unknown scenario: " + id);
        }
        this.activeScenarioId = id;
    }
}

