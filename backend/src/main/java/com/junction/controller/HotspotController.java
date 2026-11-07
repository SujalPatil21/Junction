package com.junction.controller;

import com.junction.dto.CascadeAnalysisDto;
import com.junction.dto.HotspotPredictionDto;
import com.junction.dto.ZoneStateDto;
import com.junction.service.HotspotAndCascadeService;
import com.junction.service.IngestionPipelineService;
import com.junction.service.ScenarioService;
import com.junction.service.SensorFusionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotspots")
@Tag(name = "Hotspots", description = "Spatial Pressure & Cascade Hotspot Detection")
public class HotspotController {

    private final HotspotAndCascadeService hotspotService;
    private final SensorFusionService sensorFusionService;
    private final IngestionPipelineService ingestionPipeline;
    private final ScenarioService scenarioService;

    public HotspotController(
        HotspotAndCascadeService hotspotService,
        SensorFusionService sensorFusionService,
        IngestionPipelineService ingestionPipeline,
        ScenarioService scenarioService
    ) {
        this.hotspotService = hotspotService;
        this.sensorFusionService = sensorFusionService;
        this.ingestionPipeline = ingestionPipeline;
        this.scenarioService = scenarioService;
    }

    @GetMapping
    @Operation(summary = "Detect active hotspots and cascade risks")
    public ResponseEntity<Map<String, Object>> getHotspots(
        @RequestParam(required = false) String scenario
    ) {
        var obs = ingestionPipeline.getRecentObservations();
        List<ZoneStateDto> zones = sensorFusionService.fuseAllZones(obs);
        String activeScenario = scenario != null ? scenario : scenarioService.getActiveScenarioId();

        List<HotspotPredictionDto> hotspots = hotspotService.detectHotspots(zones);
        CascadeAnalysisDto cascade = hotspotService.analyzeCascade(
            zones.isEmpty() ? "ZONE_WANKHEDE" : zones.get(0).getId(),
            zones.isEmpty() ? 50 : zones.get(0).getPressure()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("scenario", activeScenario);
        response.put("timestamp", Instant.now().toString());
        response.put("hotspotCount", hotspots.size());
        response.put("hotspotPredictions", hotspots);
        response.put("cascadeAnalysis", cascade);

        return ResponseEntity.ok(response);
    }
}
