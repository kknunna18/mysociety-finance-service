package com.mysociety.finance.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "mysociety")
public class Invoice {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "society_id", nullable = false)
    private UUID societyId;
    @Column(name = "billing_run_id")
    private UUID billingRunId;
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;
    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;
    @Column(name = "billing_period", nullable = false)
    private String billingPeriod;
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "INR";
    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "adjustment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    @Column(name = "outstanding_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingAmount;
    @Column(nullable = false)
    private String status = "DRAFT";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Version
    private long version;

    public UUID getId() {
        return id;
    }

    public UUID getSocietyId() {
        return societyId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public String getStatus() {
        return status;
    }

    @PreUpdate
    void updated() {
        updatedAt = Instant.now();
    }
}
