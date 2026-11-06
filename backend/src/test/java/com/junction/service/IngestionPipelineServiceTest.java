package com.junction.service;

import com.junction.dto.BatchIngestionRequestDto;
import com.junction.dto.BatchIngestionResponseDto;
import com.junction.dto.NormalizedObservationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngestionPipelineServiceTest {

    private IngestionPipelineService pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new IngestionPipelineService(2000, 180, 0.7, 200);
    }

    @Test
    @DisplayName("Should accept valid observation batch and calculate confidence")
    void testAcceptValidBatch() {
        NormalizedObservationDto obs = NormalizedObservationDto.builder()
            .id("OBS_TEST_001")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .resourceId("WANKHEDE_EXIT")
            .metricType("CROWD_COUNT")
            .value(1200.0)
            .unit("persons")
            .confidence(0.9)
            .observedAt(Instant.now().toString())
            .build();

        BatchIngestionResponseDto response = pipeline.processBatch(new BatchIngestionRequestDto(List.of(obs)));

        assertEquals(1, response.getAcceptedCount());
        assertEquals(0, response.getQuarantinedCount());
        assertEquals(0, response.getDeduplicatedCount());
        assertEquals(0.9, response.getAccepted().get(0).getConfidence());
    }

    @Test
    @DisplayName("Should quarantine duplicate observation IDs")
    void testDeduplicateObservations() {
        NormalizedObservationDto obs1 = NormalizedObservationDto.builder()
            .id("OBS_DUP_001")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .value(500.0)
            .observedAt(Instant.now().toString())
            .build();

        pipeline.processBatch(new BatchIngestionRequestDto(List.of(obs1)));

        // Second batch with duplicate ID
        BatchIngestionResponseDto response2 = pipeline.processBatch(new BatchIngestionRequestDto(List.of(obs1)));

        assertEquals(0, response2.getAcceptedCount());
        assertEquals(1, response2.getDeduplicatedCount());
        assertEquals(1, response2.getQuarantinedCount());
    }

    @Test
    @DisplayName("Should downgrade confidence by 0.7x for observations older than 180 seconds")
    void testStaleObservationConfidenceDowngrade() {
        String staleTime = Instant.now().minusSeconds(200).toString();

        NormalizedObservationDto obs = NormalizedObservationDto.builder()
            .id("OBS_STALE_001")
            .sourceId("DEV_CCTV_WANKHEDE_01")
            .zoneId("ZONE_WANKHEDE")
            .value(800.0)
            .confidence(0.9)
            .qualityStatus("FRESH")
            .observedAt(staleTime)
            .build();

        BatchIngestionResponseDto response = pipeline.processBatch(new BatchIngestionRequestDto(List.of(obs)));

        assertEquals(1, response.getAcceptedCount());
        NormalizedObservationDto accepted = response.getAccepted().get(0);
        assertEquals("STALE", accepted.getQualityStatus());
        assertEquals(0.9 * 0.7, accepted.getConfidence(), 0.001);
    }

    @Test
    @DisplayName("Should quarantine observations missing mandatory fields")
    void testMissingMandatoryFields() {
        NormalizedObservationDto missingId = NormalizedObservationDto.builder()
            .sourceId("DEV_01")
            .zoneId("ZONE_WANKHEDE")
            .value(100.0)
            .build();

        BatchIngestionResponseDto response = pipeline.processBatch(new BatchIngestionRequestDto(List.of(missingId)));

        assertEquals(0, response.getAcceptedCount());
        assertEquals(1, response.getQuarantinedCount());
        assertTrue(response.getQuarantined().get(0).get("reason").toString().contains("Missing mandatory"));
    }
}

