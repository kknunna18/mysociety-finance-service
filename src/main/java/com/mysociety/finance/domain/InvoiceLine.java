package com.mysociety.finance.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoice_lines", schema = "mysociety")
public class InvoiceLine {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "society_id", nullable = false)
    private UUID societyId;
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;
    @Column(name = "charge_head_id")
    private UUID chargeHeadId;
    @Column(name = "line_number", nullable = false)
    private int lineNumber;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    @Column(name = "unit_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitRate;
    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxableAmount;
    @Column(name = "tax_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxPercentage;
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;
    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
