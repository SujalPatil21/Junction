package com.junction.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for the JUNCTION backend.
 *
 * <p>Cache specifications mirror the TypeScript ingestion pipeline constants:
 * <ul>
 *   <li>{@code DEDUP_CACHE_MAX_SIZE = 2000} — LRU eviction bound</li>
 *   <li>{@code STALE_THRESHOLD_SECONDS = 180} — 3-minute TTL</li>
 * </ul>
 *
 * <p>Source of truth: {@code src/services/ingestionPipeline.ts}
 */
@Configuration
public class CacheConfig {

    /** Maximum number of entries in the ingestion deduplication cache (mirrors TS: 2000). */
    @Value("${junction.ingestion.dedup-cache-max-size:2000}")
    private int dedupCacheMaxSize;

    /** Stale observation threshold in seconds (mirrors TS: 180s). */
    @Value("${junction.ingestion.stale-threshold-seconds:180}")
    private long staleThresholdSeconds;

    /**
     * Cache name constants used across the application.
     * Centralised here to avoid string literals scattered in service classes.
     */
    public static final String CACHE_OBSERVATION_DEDUP = "observationDedup";
    public static final String CACHE_ZONE_STATE         = "zoneState";
    public static final String CACHE_DEVICE_REGISTRY    = "deviceRegistry";
    public static final String CACHE_SCENARIO           = "scenarios";

    /**
     * Primary CacheManager backed by Caffeine.
     *
     * <p>Cache specifications:
     * <ul>
     *   <li>{@code observationDedup} — LRU 2000 entries, 180s TTL (write), no refresh</li>
     *   <li>{@code zoneState} — LRU 100 entries, 30s TTL (fused state snapshots)</li>
     *   <li>{@code deviceRegistry} — LRU 500 entries, 300s TTL (device metadata)</li>
     *   <li>{@code scenarios} — LRU 50 entries, 60s TTL (scenario definitions)</li>
     * </ul>
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setAllowNullValues(false);

        // Each cache gets its own Caffeine spec.
        // observationDedup: mirrors TypeScript LRU Map(2000), stale threshold 180s
        manager.registerCustomCache(
            CACHE_OBSERVATION_DEDUP,
            Caffeine.newBuilder()
                .maximumSize(dedupCacheMaxSize)
                .expireAfterWrite(staleThresholdSeconds, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        // zoneState: fused zone snapshots — short TTL to reflect near-real-time sensor data
        manager.registerCustomCache(
            CACHE_ZONE_STATE,
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        // deviceRegistry: hardware node metadata — longer TTL, rarely changes
        manager.registerCustomCache(
            CACHE_DEVICE_REGISTRY,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        // scenarios: event scenario definitions
        manager.registerCustomCache(
            CACHE_SCENARIO,
            Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        return manager;
    }
}

