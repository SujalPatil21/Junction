package com.junction.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.junction.dto.BatchIngestionRequestDto;
import com.junction.dto.BatchIngestionResponseDto;
import com.junction.dto.NormalizedObservationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Service
public class IngestionPipelineService {

    private final Cache<String, Boolean> dedupCache;
    private final Deque<NormalizedObservationDto> ringBuffer = new ConcurrentLinkedDeque<>();
    private final int ringBufferCapacity;
    private final long staleThresholdSeconds;
    private final double staleConfidenceMultiplier;

    public IngestionPipelineService(
        @Value("${junction.ingestion.dedup-cache-max-size:2000}") int dedupCacheMaxSize,
        @Value("${junction.ingestion.stale-threshold-seconds:180}") long staleThresholdSeconds,
        @Value("${junction.ingestion.stale-confidence-multiplier:0.7}") double staleConfidenceMultiplier,
        @Value("${junction.ingestion.ring-buffer-capacity:200}") int ringBufferCapacity
    ) {
        this.staleThresholdSeconds = staleThresholdSeconds;
        this.staleConfidenceMultiplier = staleConfidenceMultiplier;
        this.ringBufferCapacity = ringBufferCapacity;
        this.dedupCache = Caffeine.newBuilder()
            .maximumSize(dedupCacheMaxSize)
            .expireAfterWrite(staleThresholdSeconds, TimeUnit.SECONDS)
            .build();
    }

    public BatchIngestionResponseDto processBatch(BatchIngestionRequestDto request) {
        if (request == null || request.getObservations() == null) {
            return BatchIngestionResponseDto.builder()
                .accepted(List.of())
                .quarantined(List.of())
                .acceptedCount(0)
                .quarantinedCount(0)
                .deduplicatedCount(0)
                .build();
        }

        List<NormalizedObservationDto> accepted = new ArrayList<>();
        List<Map<String, Object>> quarantined = new ArrayList<>();
        int deduplicatedCount = 0;
        long nowMillis = System.currentTimeMillis();

        for (NormalizedObservationDto obs : request.getObservations()) {
            // 1. Mandatory Schema Validation
            if (obs.getId() == null || obs.getId().isBlank() ||
                obs.getSourceId() == null || obs.getSourceId().isBlank() ||
                obs.getZoneId() == null || obs.getZoneId().isBlank() ||
                obs.getValue() == null) {
                quarantined.add(Map.of(
                    "observation", obs,
                    "reason", "Missing mandatory fields (id, sourceId, zoneId, or value)"
                ));
                continue;
            }

            // 2. Range & Sanity Validation
            if (obs.getValue().isNaN() || obs.getValue() < 0) {
                quarantined.add(Map.of(
                    "observation", obs,
                    "reason", "Invalid value: negative or NaN count detected"
                ));
                continue;
            }

            // 3. Deduplication Check
            if (dedupCache.getIfPresent(obs.getId()) != null) {
                deduplicatedCount++;
                quarantined.add(Map.of(
                    "observation", obs,
                    "reason", "Duplicate observation ID detected in cache"
                ));
                continue;
            }

            // 4. Freshness Scoring
            String qualityStatus = obs.getQualityStatus() != null ? obs.getQualityStatus() : "FRESH";
            int ageSeconds = 0;
            if (obs.getObservedAt() != null) {
                try {
                    long observedTime = Instant.parse(obs.getObservedAt()).toEpochMilli();
                    ageSeconds = (int) Math.max(0, (nowMillis - observedTime) / 1000);
                } catch (Exception ignored) {
                    ageSeconds = 0;
                }
            }

            if (ageSeconds > staleThresholdSeconds && "FRESH".equals(qualityStatus)) {
                qualityStatus = "STALE";
            }

            // 5. Confidence Bounds Check
            double baseConfidence = obs.getConfidence() != null ? obs.getConfidence() : 0.8;
            double clampedConfidence = Math.max(0.1, Math.min(1.0, baseConfidence));
            double finalConfidence = "STALE".equals(qualityStatus)
                ? clampedConfidence * staleConfidenceMultiplier
                : clampedConfidence;

            // Cache ID
            dedupCache.put(obs.getId(), Boolean.TRUE);

            NormalizedObservationDto acceptedObs = NormalizedObservationDto.builder()
                .id(obs.getId())
                .sourceId(obs.getSourceId())
                .zoneId(obs.getZoneId())
                .resourceId(obs.getResourceId())
                .metricType(obs.getMetricType() != null ? obs.getMetricType() : "CROWD_COUNT")
                .value(obs.getValue())
                .unit(obs.getUnit())
                .confidence(finalConfidence)
                .qualityStatus(qualityStatus)
                .freshnessSeconds(ageSeconds)
                .observedAt(obs.getObservedAt() != null ? obs.getObservedAt() : Instant.now().toString())
                .receivedAt(Instant.now().toString())
                .build();

            accepted.add(acceptedObs);

            // Add to ring buffer
            ringBuffer.addFirst(acceptedObs);
            while (ringBuffer.size() > ringBufferCapacity) {
                ringBuffer.pollLast();
            }
        }

        return BatchIngestionResponseDto.builder()
            .accepted(accepted)
            .quarantined(quarantined)
            .acceptedCount(accepted.size())
            .quarantinedCount(quarantined.size())
            .deduplicatedCount(deduplicatedCount)
            .build();
    }

    public List<NormalizedObservationDto> getRecentObservations() {
        return new ArrayList<>(ringBuffer);
    }

    public List<NormalizedObservationDto> getRecentObservationsForZone(String zoneId) {
        return ringBuffer.stream()
            .filter(o -> Objects.equals(o.getZoneId(), zoneId))
            .toList();
    }
}

