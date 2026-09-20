package com.mysociety.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="payment_attempts", schema="mysociety", uniqueConstraints=@UniqueConstraint(columnNames={"society_id","idempotency_key"}))
public class PaymentAttempt {
    @Id @GeneratedValue private UUID id;
    @Column(name="society_id", nullable=false) private UUID societyId;
    @Column(name="unit_id", nullable=false) private UUID unitId;
    @Column(name="initiated_by") private UUID initiatedBy;
    @Column(name="payment_reference", nullable=false) private String paymentReference;
    @Column(name="idempotency_key", nullable=false) private String idempotencyKey;
    @Column(nullable=false) private String provider;
    @Column(name="provider_order_id") private String providerOrderId;
    @Column(name="provider_payment_id") private String providerPaymentId;
    @Column(name="payment_method") private String paymentMethod;
    @Column(name="currency_code", nullable=false) private String currencyCode="INR";
    @Column(name="requested_amount", nullable=false, precision=19, scale=2) private BigDecimal requestedAmount;
    @Column(name="confirmed_amount", nullable=false, precision=19, scale=2) private BigDecimal confirmedAmount=BigDecimal.ZERO;
    @Column(name="refunded_amount", nullable=false, precision=19, scale=2) private BigDecimal refundedAmount=BigDecimal.ZERO;
    @Column(nullable=false) private String status="CREATED";
    @Column(name="failure_code") private String failureCode;
    @Column(name="failure_message") private String failureMessage;
    @Column(name="confirmed_at") private Instant confirmedAt;
    @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at", nullable=false) private Instant updatedAt=Instant.now();
    @Version private long version;
    public UUID getId(){return id;} public UUID getSocietyId(){return societyId;} public String getPaymentReference(){return paymentReference;} public String getStatus(){return status;} public BigDecimal getRequestedAmount(){return requestedAmount;}
    public static PaymentAttempt create(UUID society, UUID unit, UUID user, String ref, String key, String provider, BigDecimal amount) { PaymentAttempt p=new PaymentAttempt();p.societyId=society;p.unitId=unit;p.initiatedBy=user;p.paymentReference=ref;p.idempotencyKey=key;p.provider=provider;p.requestedAmount=amount;return p;}
    public void requestRefund() { if (!"CONFIRMED".equals(status)) throw new IllegalStateException("Only confirmed payments may be refunded"); status="REFUND_PENDING"; }
    public void confirm(String providerPaymentId, BigDecimal amount) { if (amount.compareTo(requestedAmount)>0) throw new IllegalArgumentException("Confirmed amount exceeds requested amount"); this.providerPaymentId=providerPaymentId; confirmedAmount=amount; status="CONFIRMED"; confirmedAt=Instant.now(); }
    @PreUpdate void updated(){updatedAt=Instant.now();}
}
