package com.junction.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converts a Clerk-issued JWT into a Spring Security {@link AbstractAuthenticationToken}.
 *
 * <p><strong>Clerk JWT Claim Structure:</strong>
 * <pre>
 * {
 *   "sub":       "user_2abc123...",        // Clerk user ID (maps to profiles.clerk_user_id)
 *   "iss":       "https://your.clerk.accounts.dev",
 *   "iat":       1700000000,
 *   "exp":       1700003600,
 *   "email":     "user@example.com",       // Clerk built-in claim
 *   "metadata":  {                         // Public metadata set by backend on profile creation
 *     "role":    "ORGANIZER"               // Junction domain role (set server-side only)
 *   }
 * }
 * </pre>
 *
 * <p><strong>Authority Mapping:</strong>
 * <ul>
 *   <li>{@code metadata.role = "ORGANIZER"} → {@code ROLE_ORGANIZER}</li>
 *   <li>{@code metadata.role = "RESTAURANT_PARTNER"} → {@code ROLE_RESTAURANT_PARTNER}</li>
 *   <li>{@code metadata.role = "ATTENDEE"} → {@code ROLE_ATTENDEE}</li>
 *   <li>No role claim → {@code ROLE_ATTENDEE} (least-privilege default)</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> The role MUST come from server-set public metadata,
 * NOT from any client-controllable claim. The backend verifies roles against the
 * {@code profiles} table on sensitive operations. Never trust client-provided role info.
 *
 * <p>Source of truth: {@code MIGRATION_READINESS_REPORT.md} Section 4 — Clerk JWT → Spring Security.
 */
@Component
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String METADATA_CLAIM = "metadata";
    private static final String ROLE_CLAIM      = "role";
    private static final String DEFAULT_ROLE    = "ATTENDEE";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        String role = extractRole(jwt);
        // Spring Security hasRole("ORGANIZER") checks for "ROLE_ORGANIZER" prefix
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return authorities;
    }

    /**
     * Extracts the Junction domain role from the Clerk JWT {@code metadata} claim.
     *
     * <p>Falls back to {@code ATTENDEE} if the claim is absent or invalid.
     * This is the least-privilege default — organizer access requires explicit role assignment.
     */
    @SuppressWarnings("unchecked")
    private String extractRole(Jwt jwt) {
        try {
            Object metadataClaim = jwt.getClaim(METADATA_CLAIM);
            if (metadataClaim instanceof Map<?, ?> metadata) {
                Object roleClaim = metadata.get(ROLE_CLAIM);
                if (roleClaim instanceof String role && isValidRole(role)) {
                    return role;
                }
            }
        } catch (Exception e) {
            // Log and fall through to default — do not expose token parsing errors
        }
        return DEFAULT_ROLE;
    }

    /**
     * Validates that the role string matches one of the Junction domain roles.
     * Prevents injection of arbitrary Spring Security authorities via JWT claims.
     */
    private boolean isValidRole(String role) {
        return switch (role) {
            case "ORGANIZER", "RESTAURANT_PARTNER", "ATTENDEE" -> true;
            default -> false;
        };
    }
}

