package com.mysociety.finance.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deployment placeholder: replace with a database-backed outbox publisher on the shared schema.
 * It intentionally logs only event metadata and never pretends publication succeeded.
 */
@Component
public class LoggingTransactionalOutbox implements TransactionalOutbox {
    private static final Logger log = LoggerFactory.getLogger(LoggingTransactionalOutbox.class);

    @Override
    public void append(DomainEvent event) {
        log.info("domain_event_queued type={} aggregateId={} societyId={} correlationId={}", event.eventType(), event.aggregateId(), event.societyId(), event.correlationId());
    }
}
