package com.junction.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authentication filter for edge device (cv_bridge) ingestion requests.
 *
 * <p>The Python {@code cv_bridge} sends observations to {@code POST /api/observations}
 * using a static shared secret:
 * <pre>
 *   Authorization: Bearer &lt;JUNCTION_EDGE_DEVICE_SECRET&gt;
 * </pre>
 *
 * <p>This filter intercepts only {@code POST /api/observations} requests. If the
 * Bearer token matches the configured edge device secret, it sets an
 * {@link EdgeDeviceAuthentication} in the SecurityContext, granting the
 * {@code ROLE_EDGE_DEVICE} authority.
 *
 * <p>All other paths are passed through without modification, allowing the
 * standard Clerk JWT path to handle them.
 *
 * <p><strong>Security Note:</strong>
 * <ul>
 *   <li>This filter runs BEFORE {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}</li>
 *   <li>The secret is compared using {@link org.springframework.security.crypto.codec.Utf8#encode} timing-safe comparison is NOT performed here — use a fixed-length secret of sufficient entropy (32+ chars)</li>
 *   <li>Use HTTPS in production — the secret is transmitted as a Bearer token</li>
 *   <li>Rotate the secret via environment variable; no code change required</li>
 * </ul>
 *
 * <p>Source: {@code cv_bridge/transport.py} — sends Bearer token via Authorization header.
 */
@Component
public class EdgeDeviceAuthenticationFilter extends OncePerRequestFilter {

    private static final String OBSERVATIONS_PATH = "/api/observations";
    private static final String BEARER_PREFIX      = "Bearer ";

    @Value("${junction.security.edge-device.secret}")
    private String edgeDeviceSecret;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Only process POST /api/observations
        if (!OBSERVATIONS_PATH.equals(request.getServletPath()) ||
            !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendUnauthorized(response, "Edge device authentication required");
            return;
        }

        String providedSecret = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Constant-time comparison to mitigate timing attacks
        if (!constantTimeEquals(providedSecret, edgeDeviceSecret)) {
            sendUnauthorized(response, "Invalid edge device secret");
            return;
        }

        // Authentication successful — set EdgeDeviceAuthentication in context
        EdgeDeviceAuthentication auth = new EdgeDeviceAuthentication(
            extractDeviceId(request),
            List.of(new SimpleGrantedAuthority("ROLE_EDGE_DEVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the optional device ID from the custom {@code X-Edge-Device-Id} header.
     * Falls back to {@code "unknown"} if the header is absent.
     */
    private String extractDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Edge-Device-Id");
        return (deviceId != null && !deviceId.isBlank()) ? deviceId.trim() : "unknown";
    }

    /**
     * Constant-time string comparison to prevent timing-based secret discovery.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        if (aBytes.length != bBytes.length) {
            // Still iterate full length to prevent length-based timing leaks
            int diff = 0;
            for (int i = 0; i < Math.max(aBytes.length, bBytes.length); i++) {
                byte ba = i < aBytes.length ? aBytes[i] : 0;
                byte bb = i < bBytes.length ? bBytes[i] : 0;
                diff |= ba ^ bb;
            }
            return diff == 0 && aBytes.length == bBytes.length;
        }

        int diff = 0;
        for (int i = 0; i < aBytes.length; i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"error\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}"
        );
    }
}

