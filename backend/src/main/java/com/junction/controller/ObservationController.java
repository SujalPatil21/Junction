package com.junction.controller;

import com.junction.dto.BatchIngestionRequestDto;
import com.junction.dto.BatchIngestionResponseDto;
import com.junction.dto.NormalizedObservationDto;
import com.junction.service.IngestionPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observations")
@Tag(name = "Observations", description = "Multi-modal sensor & CV observation ingestion pipeline")
public class ObservationController {

    private final IngestionPipelineService ingestionPipeline;

    public ObservationController(IngestionPipelineService ingestionPipeline) {
        this.ingestionPipeline = ingestionPipeline;
    }

    @PostMapping
    @Operation(summary = "Ingest batch of normalized observations (cv_bridge edge device or service)")
    public ResponseEntity<BatchIngestionResponseDto> ingestBatch(@Valid @RequestBody BatchIngestionRequestDto request) {
        BatchIngestionResponseDto result = ingestionPipeline.processBatch(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get recent ingested observations buffer")
    public ResponseEntity<List<NormalizedObservationDto>> getRecentObservations(
        @RequestParam(required = false) String zoneId
    ) {
        if (zoneId != null && !zoneId.isBlank()) {
            return ResponseEntity.ok(ingestionPipeline.getRecentObservationsForZone(zoneId));
        }
        return ResponseEntity.ok(ingestionPipeline.getRecentObservations());
    }
}

