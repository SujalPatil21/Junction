package com.junction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the JUNCTION backend.
 *
 * <p>Allowed origins are configured per environment via the
 * {@code junction.cors.allowed-origins} property (comma-separated).
 *
 * <p>Dev defaults: {@code http://localhost:3000,http://localhost:3001}
 * (Next.js frontend ports)
 *
 * <p>Production: Set {@code CORS_ALLOWED_ORIGINS} environment variable to the
 * deployed frontend URL (e.g., {@code https://junction-app.up.railway.app}).
 */
@Configuration
public class CorsConfig {

    @Value("${junction.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsRaw;

    /**
     * CORS configuration source registered with Spring Security.
     * Applied globally to all {@code /api/**} and {@code /actuator/**} endpoints.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parse comma-separated allowed origins from environment
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

        config.setAllowedOrigins(origins);

        // HTTP methods needed by the Junction API
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers needed by Clerk JWT auth and standard browser requests
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control",
            "X-Edge-Device-Id"   // Custom header for cv_bridge edge device identification
        ));

        config.setExposedHeaders(List.of(
            "X-Request-Id",
            "X-Rate-Limit-Remaining"
        ));

        // Allow credentials (Clerk session cookies + Authorization header)
        config.setAllowCredentials(true);

        // Preflight cache: 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

