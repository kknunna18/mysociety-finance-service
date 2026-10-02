package com.mysociety.finance.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class TenantContextResolverTest {
    @Test
    void extractsSocietyOnlyFromJwtClaims() {
        UUID society = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", user.toString(), "society_id", society.toString()));
        TenantContext context = new TenantContextResolver().current(new JwtAuthenticationToken(jwt), "cid");
        assertThat(context.societyId()).isEqualTo(society);
    }
}
