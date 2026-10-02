package com.mysociety.finance.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "billing_runs", schema = "mysociety")
public class BillingRun {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "society_id", nullable = false)
    private UUID societyId;
    @Column(name = "run_number", nullable = false)
    private String runNumber;
    @Column(name = "billing_period", nullable = false)
    private String billingPeriod;
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(nullable = false)
    private String status = "DRAFT";
    @Column(name = "total_units", nullable = false)
    private int totalUnits;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "published_by")
    private UUID publishedBy;
    @Column(name = "published_at")
    private Instant publishedAt;
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

    public String getStatus() {
        return status;
    }

    public static BillingRun create(UUID societyId, String number, String period, LocalDate start, LocalDate end, LocalDate due, UUID user) {
        BillingRun r = new BillingRun();
        r.societyId = societyId;
        r.runNumber = number;
        r.billingPeriod = period;
        r.periodStart = start;
        r.periodEnd = end;
        r.dueDate = due;
        r.createdBy = user;
        return r;
    }

    public void publish(UUID user) {
        if (!"READY".equals(status) && !"DRAFT".equals(status))
            throw new IllegalStateException("Billing run cannot be published from " + status);
        status = "PUBLISHED";
        publishedBy = user;
        publishedAt = Instant.now();
    }

    @PreUpdate
    void updated() {
        updatedAt = Instant.now();
    }
}
