package com.mysociety.finance.outbox;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(UUID eventId, String eventType, int eventVersion, Instant occurredAt, UUID societyId,
                          String aggregateType, UUID aggregateId, String correlationId, Object payload) {
}
