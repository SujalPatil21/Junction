package com.junction.controller;

import com.junction.dto.ResourceForecastDto;
import com.junction.dto.ZoneStateDto;
import com.junction.service.ForecastingService;
import com.junction.service.IngestionPipelineService;
import com.junction.service.ScenarioService;
import com.junction.service.SensorFusionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/forecast", "/api/predictions"})
@Tag(name = "Forecast", description = "Short-horizon crowd surge forecasting")
public class ForecastController {

    private final ForecastingService forecastingService;
    private final SensorFusionService sensorFusionService;
    private final IngestionPipelineService ingestionPipeline;
    private final ScenarioService scenarioService;

    public ForecastController(
        ForecastingService forecastingService,
        SensorFusionService sensorFusionService,
        IngestionPipelineService ingestionPipeline,
        ScenarioService scenarioService
    ) {
        this.forecastingService = forecastingService;
        this.sensorFusionService = sensorFusionService;
        this.ingestionPipeline = ingestionPipeline;
        this.scenarioService = scenarioService;
    }

    @GetMapping
    @Operation(summary = "Generate resource / zone crowd forecasts")
    public ResponseEntity<List<ResourceForecastDto>> getForecasts() {
        var obs = ingestionPipeline.getRecentObservations();
        List<ZoneStateDto> zones = sensorFusionService.fuseAllZones(obs);
        String scenario = scenarioService.getActiveScenarioId();

        List<ResourceForecastDto> forecasts = zones.stream()
            .map(z -> forecastingService.generateForecast(
                z.getId(),
                z.getName(),
                z.getId(),
                z.getPressure(),
                scenario,
                z.getConfidence()
            ))
            .toList();

        return ResponseEntity.ok(forecasts);
    }
}

