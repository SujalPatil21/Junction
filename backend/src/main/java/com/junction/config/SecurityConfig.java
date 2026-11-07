package com.junction.config;

import com.junction.security.EdgeDeviceAuthentication;
import com.junction.security.EdgeDeviceAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the JUNCTION backend.
 *
 * <p><strong>Security Model:</strong>
 * <ol>
 *   <li><strong>Public endpoints</strong> — no authentication required</li>
 *   <li><strong>Authenticated endpoints</strong> — valid Clerk JWT required</li>
 *   <li><strong>Organizer-only mutations</strong> — Clerk JWT + {@code ROLE_ORGANIZER} authority</li>
 *   <li><strong>Edge device ingestion</strong> — static API key (for cv_bridge POST /api/observations)</li>
 * </ol>
 *
 * <p><strong>API Security Matrix (from MIGRATION_READINESS_REPORT.md):</strong>
 * <pre>
 * PUBLIC (no auth):
 *   GET  /actuator/health
 *   GET  /api/health
 *   GET  /api/scenarios
 *
 * AUTHENTICATED (Clerk JWT):
 *   GET  /api/zones
 *   GET  /api/zones/{zoneId}
 *   GET  /api/recommendations
 *   GET  /api/routing/**
 *   GET  /api/hospitality/demand
 *   GET  /api/forecast
 *   POST /api/simulation/**
 *
 * ORGANIZER ONLY (Clerk JWT + ROLE_ORGANIZER):
 *   POST   /api/recommendations/{id}/approve
 *   POST   /api/recommendations/{id}/reject
 *   POST   /api/recommendations/{id}/execute
 *   POST   /api/scenarios
 *   DELETE /api/scenarios/{id}
 *   POST   /api/restaurants
 *   PUT    /api/restaurants/{id}
 *   POST   /api/restaurants/{id}/inventory
 *
 * EDGE DEVICE (static API key — X-Edge-Device header):
 *   POST /api/observations
 * </pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtDecoder jwtDecoder;
    private final ClerkJwtAuthenticationConverter clerkJwtAuthenticationConverter;
    private final EdgeDeviceAuthenticationFilter edgeDeviceAuthenticationFilter;

    @Autowired
    public SecurityConfig(
        CorsConfigurationSource corsConfigurationSource,
        JwtDecoder jwtDecoder,
        ClerkJwtAuthenticationConverter clerkJwtAuthenticationConverter,
        EdgeDeviceAuthenticationFilter edgeDeviceAuthenticationFilter
    ) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtDecoder = jwtDecoder;
        this.clerkJwtAuthenticationConverter = clerkJwtAuthenticationConverter;
        this.edgeDeviceAuthenticationFilter = edgeDeviceAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Stateless REST API — disable session and CSRF ──
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS — configured in CorsConfig.java ──
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // ── Edge device filter (before standard auth) ──
            // Processes POST /api/observations with Bearer <JUNCTION_EDGE_DEVICE_SECRET>
            .addFilterBefore(edgeDeviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // ── Authorization rules ──
            .authorizeHttpRequests(auth -> auth

                // Public — health checks (Railway + uptime monitors)
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()

                // Public — scenario read (attendee Flutter app, unauthenticated map view)
                .requestMatchers(HttpMethod.GET, "/api/scenarios").permitAll()

                // Edge device — POST /api/observations (handled by EdgeDeviceAuthenticationFilter)
                // The filter sets authentication before this rule is evaluated.
                // We require authentication here; the filter provides EdgeDeviceAuthentication.
                .requestMatchers(HttpMethod.POST, "/api/observations").authenticated()

                // Organizer-only mutations
                .requestMatchers(HttpMethod.POST,   "/api/recommendations/*/approve").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/recommendations/*/reject").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/recommendations/*/execute").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/scenarios").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.DELETE, "/api/scenarios/*").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/restaurants").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.PUT,    "/api/restaurants/*").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/restaurants/*/inventory").hasRole("ORGANIZER")

                // All other /api/** require authentication
                .requestMatchers("/api/**").authenticated()

                // OpenAPI / Swagger UI (dev only — disabled in prod via application-prod.yml)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Deny everything else
                .anyRequest().denyAll()
            )

            // ── JWT Resource Server (Clerk RS256 tokens) ──
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(request -> {
                    String uri = request.getRequestURI();
                    if (uri != null && uri.contains("/api/observations")) {
                        return null;
                    }
                    if (SecurityContextHolder.getContext().getAuthentication() instanceof EdgeDeviceAuthentication) {
                        return null;
                    }
                    return new DefaultBearerTokenResolver().resolve(request);
                })
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(clerkJwtAuthenticationConverter)
                )
            );

        return http.build();
    }
}

