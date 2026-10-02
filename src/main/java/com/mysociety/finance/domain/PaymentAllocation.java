package com.mysociety.finance.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations", schema = "mysociety")
public class PaymentAllocation {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "society_id", nullable = false)
    private UUID societyId;
    @Column(name = "payment_attempt_id", nullable = false)
    private UUID paymentAttemptId;
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;
    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedAmount;
    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt = Instant.now();
    @Column(name = "reversed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal reversedAmount = BigDecimal.ZERO;

    public static PaymentAllocation create(UUID society, UUID payment, UUID invoice, BigDecimal amount) {
        PaymentAllocation a = new PaymentAllocation();
        a.societyId = society;
        a.paymentAttemptId = payment;
        a.invoiceId = invoice;
        a.allocatedAmount = amount;
        return a;
    }
}
