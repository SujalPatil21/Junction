package com.junction.controller;

import com.junction.dto.ScenarioDto;
import com.junction.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/scenarios")
@Tag(name = "Scenarios", description = "Operational scenarios and event simulation triggers")
public class ScenarioController {

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping
    @Operation(summary = "Get all scenarios and the active scenario ID (Public - contract compatible with Flutter & Web)")
    public ResponseEntity<Map<String, Object>> getScenarios() {
        String active = scenarioService.getActiveScenarioId();
        List<ScenarioDto> all = scenarioService.getAllScenarios();

        List<Map<String, String>> available = all.stream()
            .map(s -> Map.of("id", s.getId(), "label", s.getLabel(), "description", s.getDescription()))
            .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("activeScenario", active);
        response.put("activeScenarioId", active);
        response.put("scenarioVersion", 1);
        response.put("isRec1Approved", false);
        response.put("scenarios", all);
        response.put("availableScenarios", available);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Set active scenario (Organizer only)")
    public ResponseEntity<Map<String, Object>> setActiveScenario(@RequestBody Map<String, String> body) {
        String scenarioId = body.get("scenarioId");
        if (scenarioId == null || scenarioId.isBlank()) {
            scenarioId = body.get("activeScenario");
        }
        if (scenarioId == null || scenarioId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "scenarioId is required"));
        }
        scenarioService.setActiveScenarioId(scenarioId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "activeScenario", scenarioId,
            "activeScenarioId", scenarioId,
            "scenarioVersion", 2,
            "timestamp", Instant.now().toString()
        ));
    }
}
