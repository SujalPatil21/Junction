package com.junction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JUNCTION Backend — Spring Boot 4.1.2 Application Entry Point.
 *
 * <p>This is the sole application backend for the JUNCTION Intelligent Destination
 * Orchestration Platform. It replaces all Next.js API route handlers (src/app/api/**).
 *
 * <p>Architecture:
 * <ul>
 *   <li>Next.js — Frontend/presentation ONLY</li>
 *   <li>Spring Boot (this) — All backend business logic, persistence, security</li>
 *   <li>Python cv_bridge — Computer vision edge processing (unchanged)</li>
 *   <li>Flutter attendee_flutter_app — Mobile application (unchanged)</li>
 * </ul>
 *
 * <p>Enabled capabilities:
 * <ul>
 *   <li>{@code @EnableCaching} — Caffeine-backed LRU cache for ingestion deduplication</li>
 *   <li>{@code @EnableAsync} — Async task execution for observation ingestion pipeline</li>
 *   <li>{@code @EnableScheduling} — Scheduled tasks (simulation tick, forecast refresh)</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class JunctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(JunctionApplication.class, args);
    }
}

