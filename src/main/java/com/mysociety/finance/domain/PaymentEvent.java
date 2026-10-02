package com.mysociety.finance.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_events", schema = "mysociety")
public class PaymentEvent {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "society_id", nullable = false)
    private UUID societyId;
    @Column(name = "payment_attempt_id")
    private UUID paymentAttemptId;
    @Column(nullable = false)
    private String provider;
    @Column(name = "provider_event_id", nullable = false)
    private String providerEventId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "signature_valid", nullable = false)
    private boolean signatureValid;
    @Column(name = "event_timestamp")
    private Instant eventTimestamp;
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;
    @Column(name = "processing_status", nullable = false)
    private String processingStatus = "RECEIVED";
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    public static PaymentEvent received(UUID society, UUID attempt, String provider, String eventId, String type, boolean valid, String payload) {
        PaymentEvent e = new PaymentEvent();
        e.societyId = society;
        e.paymentAttemptId = attempt;
        e.provider = provider;
        e.providerEventId = eventId;
        e.eventType = type;
        e.signatureValid = valid;
        e.payload = payload;
        return e;
    }

    public void processed() {
        processingStatus = "PROCESSED";
    }
}
