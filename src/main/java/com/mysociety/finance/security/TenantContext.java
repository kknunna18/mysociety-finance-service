package com.mysociety.finance.security;

import java.util.UUID;
public record TenantContext(UUID societyId, UUID userId, String correlationId) {}
