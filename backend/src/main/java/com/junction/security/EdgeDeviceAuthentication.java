package com.junction.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Authentication token representing an authenticated edge device (cv_bridge).
 *
 * <p>Set by {@link EdgeDeviceAuthenticationFilter} after successful
 * Bearer secret verification.
 *
 * <p>The principal is the device ID extracted from the {@code X-Edge-Device-Id}
 * header, or {@code "unknown"} if the header is absent.
 */
public class EdgeDeviceAuthentication extends AbstractAuthenticationToken {

    private final String deviceId;

    public EdgeDeviceAuthentication(String deviceId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.deviceId = deviceId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        // Secret is not stored in the authentication token
        return null;
    }

    @Override
    public Object getPrincipal() {
        return deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}

