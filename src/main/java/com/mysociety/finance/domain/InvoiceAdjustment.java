package com.mysociety.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="invoice_adjustments", schema="mysociety")
public class InvoiceAdjustment {
    @Id @GeneratedValue private UUID id;
    @Column(name="society_id", nullable=false) private UUID societyId;
    @Column(name="invoice_id", nullable=false) private UUID invoiceId;
    @Column(name="adjustment_no", nullable=false) private String adjustmentNo;
    @Column(name="adjustment_type", nullable=false) private String adjustmentType;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    @Column(nullable=false) private String reason;
    @Column(nullable=false) private String status="PENDING";
    @Column(name="requested_by") private UUID requestedBy;
    @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at", nullable=false) private Instant updatedAt=Instant.now();
    @Version private long version;
    public static InvoiceAdjustment create(UUID society, UUID invoice, String no, String type, BigDecimal amount, String reason, UUID user) { InvoiceAdjustment a=new InvoiceAdjustment();a.societyId=society;a.invoiceId=invoice;a.adjustmentNo=no;a.adjustmentType=type;a.amount=amount;a.reason=reason;a.requestedBy=user;return a;}
}
