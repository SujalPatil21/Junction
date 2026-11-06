package com.junction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * JWT decoder configuration for Clerk-issued JWTs.
 *
 * <p>JUNCTION uses Clerk as the identity provider. Clerk signs JWTs with RS256
 * using a rotating keypair published at the JWKS URI. Spring Security's
 * {@link NimbusJwtDecoder} fetches and caches the public keys automatically.
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code CLERK_JWKS_URI} — JWKS endpoint, e.g. {@code https://your-domain.clerk.accounts.dev/.well-known/jwks.json}</li>
 *   <li>{@code CLERK_ISSUER_URI} — Expected issuer, e.g. {@code https://your-domain.clerk.accounts.dev}</li>
 * </ul>
 */
@Configuration
public class JwtConfig {

    @Value("${junction.security.clerk.jwks-uri}")
    private String jwksUri;

    /**
     * JWT decoder that verifies Clerk RS256 tokens against the Clerk JWKS endpoint.
     *
     * <p>Verification steps (Nimbus performs automatically):
     * <ol>
     *   <li>Fetch public keys from {@code CLERK_JWKS_URI}</li>
     *   <li>Verify signature using the matching {@code kid} key</li>
     *   <li>Verify {@code exp} (not expired)</li>
     *   <li>Verify {@code nbf} (not before, if present)</li>
     *   <li>Issuer validation is performed by {@code SecurityConfig} via {@code jwtDecoder}</li>
     * </ol>
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to configure JWT decoder. Verify CLERK_JWKS_URI is a valid URL: " + jwksUri, e
            );
        }
    }
}

