package com.junction.controller;

import com.junction.dto.InterventionActionRequestDto;
import com.junction.dto.OperationalInterventionDto;
import com.junction.dto.ZoneStateDto;
import com.junction.service.IngestionPipelineService;
import com.junction.service.RecommendationLifecycleService;
import com.junction.service.SensorFusionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Operational recommendations and intervention lifecycle")
public class RecommendationController {

    private final RecommendationLifecycleService recommendationService;
    private final SensorFusionService sensorFusionService;
    private final IngestionPipelineService ingestionPipeline;

    public RecommendationController(
        RecommendationLifecycleService recommendationService,
        SensorFusionService sensorFusionService,
        IngestionPipelineService ingestionPipeline
    ) {
        this.recommendationService = recommendationService;
        this.sensorFusionService = sensorFusionService;
        this.ingestionPipeline = ingestionPipeline;
    }

    @GetMapping
    @Operation(summary = "Get all operational interventions")
    public ResponseEntity<List<OperationalInterventionDto>> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getAllInterventions());
    }

    @PostMapping("/generate")
    @Operation(summary = "Evaluate zone states and generate recommendations if thresholds exceeded")
    public ResponseEntity<List<OperationalInterventionDto>> generateRecommendations() {
        var obs = ingestionPipeline.getRecentObservations();
        List<ZoneStateDto> zones = sensorFusionService.fuseAllZones(obs);
        return ResponseEntity.ok(recommendationService.generateInterventions(zones));
    }

    @PostMapping("/plan")
    @Operation(summary = "Generate multi-horizon AI recommendation plan from operational context")
    public ResponseEntity<Map<String, Object>> planRecommendations(@RequestBody(required = false) Map<String, Object> context) {
        return ResponseEntity.ok(recommendationService.planRecommendations(context));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Approve a proposed recommendation (Organizer only)")
    public ResponseEntity<OperationalInterventionDto> approve(
        @PathVariable String id,
        @RequestBody(required = false) InterventionActionRequestDto body,
        Authentication auth
    ) {
        String actor = auth != null ? auth.getName() : "ORGANIZER";
        String rationale = (body != null && body.getRationale() != null) ? body.getRationale() : "Approved by operator";
        return ResponseEntity.ok(recommendationService.approve(id, actor, rationale));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Reject a proposed recommendation (Organizer only)")
    public ResponseEntity<OperationalInterventionDto> reject(
        @PathVariable String id,
        @RequestBody(required = false) InterventionActionRequestDto body,
        Authentication auth
    ) {
        String actor = auth != null ? auth.getName() : "ORGANIZER";
        String reason = (body != null && body.getReason() != null) ? body.getReason() : "Rejected by operator";
        return ResponseEntity.ok(recommendationService.reject(id, actor, reason));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Execute an approved recommendation (Organizer only)")
    public ResponseEntity<OperationalInterventionDto> execute(
        @PathVariable String id,
        Authentication auth
    ) {
        String actor = auth != null ? auth.getName() : "ORGANIZER";
        return ResponseEntity.ok(recommendationService.execute(id, actor));
    }
}

