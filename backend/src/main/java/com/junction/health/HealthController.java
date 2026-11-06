package com.junction.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public health check endpoint for the JUNCTION backend.
 *
 * <p>This endpoint is intentionally public (no authentication required) to allow:
 * <ul>
 *   <li>Railway platform health checks</li>
 *   <li>Uptime monitoring services</li>
 *   <li>Load balancer liveness probes</li>
 *   <li>Integration test bootstrap verification</li>
 * </ul>
 *
 * <p>The Next.js equivalent was {@code src/app/api/health/route.ts}.
 *
 * <p>Spring Actuator's {@code /actuator/health} provides more detailed health info
 * (database connectivity, disk space, etc.) — use that for internal monitoring.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Service health and liveness endpoints")
public class HealthController {

    @Value("${spring.application.name:junction-backend}")
    private String applicationName;

    @Value("${info.app.version:1.0.0}")
    private String applicationVersion;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    /**
     * Returns service health status.
     *
     * <p>Response is intentionally minimal — it confirms the service is running
     * without exposing infrastructure details.
     *
     * @return 200 OK with health payload
     */
    @GetMapping
    @Operation(
        summary = "Service health check",
        description = "Returns service status. Public endpoint — no authentication required.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Service is healthy",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "status": "ok",
                          "service": "junction-backend",
                          "version": "1.0.0",
                          "environment": "prod",
                          "timestamp": "2026-11-06T17:00:00Z"
                        }
                        """)
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "junction-backend");
        response.put("version", applicationVersion);
        response.put("environment", activeProfile);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}

