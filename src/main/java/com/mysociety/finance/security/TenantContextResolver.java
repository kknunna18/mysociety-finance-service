package com.mysociety.finance.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
public class TenantContextResolver {
    public TenantContext current(Authentication authentication, String correlationId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt))
            throw new ResponseStatusException(FORBIDDEN, "JWT tenant context is required");
        try {
            return new TenantContext(UUID.fromString(jwt.getClaimAsString("society_id")), UUID.fromString(jwt.getSubject()), correlationId);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(FORBIDDEN, "JWT has no valid society context");
        }
    }
}
