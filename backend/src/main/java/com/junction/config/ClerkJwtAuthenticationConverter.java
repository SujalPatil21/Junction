package com.junction.config;

import com.junction.entity.ProfileEntity;
import com.junction.entity.RestaurantMembershipEntity;
import com.junction.entity.UserRole;
import com.junction.repository.ProfileRepository;
import com.junction.repository.RestaurantMembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import java.util.Optional;

/**
 * Converts a Clerk-issued JWT into a Spring Security {@link AbstractAuthenticationToken}.
 *
 * <p><strong>CRITICAL SECURITY ARCHITECTURE (PostgreSQL Authoritative RBAC):</strong>
 * <ul>
 *   <li>The JWT {@code sub} claim contains the Clerk user ID (e.g. {@code user_2abc123...}).</li>
 *   <li>Domain roles ({@code ORGANIZER}, {@code RESTAURANT_PARTNER}, {@code ATTENDEE}) are
 *       resolved <strong>strictly and authoritatively from PostgreSQL {@code profiles.role}</strong>.</li>
 *   <li>Client-controllable JWT claims (e.g., {@code metadata.role}) are <strong>NEVER</strong>
 *       trusted as an authority grant, preventing privilege escalation.</li>
 *   <li>If a user has no database profile, they default to {@code ROLE_ATTENDEE} (least-privilege).</li>
 *   <li>Restaurant memberships are resolved from PostgreSQL {@code restaurant_memberships} and
 *       granted as scoped authorities (e.g., {@code RESTAURANT_MEMBER_<restaurantId>}).</li>
 * </ul>
 */
@Component
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(ClerkJwtAuthenticationConverter.class);
    private static final String DEFAULT_ROLE = "ATTENDEE";

    private final ProfileRepository profileRepository;
    private final RestaurantMembershipRepository membershipRepository;

    public ClerkJwtAuthenticationConverter(
        @Lazy ProfileRepository profileRepository,
        @Lazy RestaurantMembershipRepository membershipRepository
    ) {
        this.profileRepository = profileRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String clerkUserId = jwt.getSubject();

        if (clerkUserId == null || clerkUserId.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + DEFAULT_ROLE));
            return authorities;
        }

        // Authoritative resolution from PostgreSQL profiles table
        Optional<ProfileEntity> profileOpt = profileRepository.findByClerkUserId(clerkUserId);
        if (profileOpt.isPresent()) {
            ProfileEntity profile = profileOpt.get();
            UserRole role = profile.getRole();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

            // If restaurant partner, resolve scoped restaurant memberships from DB
            if (role == UserRole.RESTAURANT_PARTNER || role == UserRole.ORGANIZER) {
                try {
                    List<RestaurantMembershipEntity> memberships = membershipRepository.findByProfileId(profile.getId());
                    for (RestaurantMembershipEntity m : memberships) {
                        authorities.add(new SimpleGrantedAuthority("RESTAURANT_MEMBER_" + m.getRestaurant().getId()));
                    }
                } catch (Exception ex) {
                    log.warn("Failed to load restaurant memberships for profile {}: {}", profile.getId(), ex.getMessage());
                }
            }
        } else {
            // Profile not yet created in PostgreSQL — assign least-privilege ATTENDEE authority
            log.debug("No database profile found for Clerk user ID: {}. Defaulting to ROLE_{}", clerkUserId, DEFAULT_ROLE);
            authorities.add(new SimpleGrantedAuthority("ROLE_" + DEFAULT_ROLE));
        }

        return authorities;
    }
}

