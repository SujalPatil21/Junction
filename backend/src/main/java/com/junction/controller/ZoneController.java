package com.junction.controller;

import com.junction.common.exception.ResourceNotFoundException;
import com.junction.dto.ZoneCoverageProfileDto;
import com.junction.dto.ZoneDefinitionDto;
import com.junction.dto.ZoneStateDto;
import com.junction.service.IngestionPipelineService;
import com.junction.service.SensorFusionService;
import com.junction.service.ZoneRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zones")
@Tag(name = "Zones", description = "Operational zones and real-time fused state")
public class ZoneController {

    private final ZoneRegistryService zoneRegistry;
    private final SensorFusionService sensorFusionService;
    private final IngestionPipelineService ingestionPipeline;

    public ZoneController(
        ZoneRegistryService zoneRegistry,
        SensorFusionService sensorFusionService,
        IngestionPipelineService ingestionPipeline
    ) {
        this.zoneRegistry = zoneRegistry;
        this.sensorFusionService = sensorFusionService;
        this.ingestionPipeline = ingestionPipeline;
    }

    @GetMapping
    @Operation(summary = "Get fused states for all registered operational zones")
    public ResponseEntity<List<ZoneStateDto>> getAllZones() {
        var observations = ingestionPipeline.getRecentObservations();
        return ResponseEntity.ok(sensorFusionService.fuseAllZones(observations));
    }

    @GetMapping("/{zoneId}")
    @Operation(summary = "Get fused state for a specific operational zone")
    public ResponseEntity<ZoneStateDto> getZoneById(@PathVariable String zoneId) {
        ZoneDefinitionDto def = zoneRegistry.getZoneById(zoneId)
            .orElseThrow(() -> new ResourceNotFoundException("Zone", zoneId));

        var observations = ingestionPipeline.getRecentObservationsForZone(zoneId);
        return ResponseEntity.ok(sensorFusionService.fuseZone(def, observations));
    }

    @GetMapping("/coverage")
    @Operation(summary = "Get data honesty coverage profiles for all zones")
    public ResponseEntity<Map<String, ZoneCoverageProfileDto>> getCoverageProfiles() {
        return ResponseEntity.ok(zoneRegistry.getAllCoverageProfiles());
    }
}

