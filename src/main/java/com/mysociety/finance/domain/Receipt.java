package com.mysociety.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="receipts", schema="mysociety")
public class Receipt {
    @Id @GeneratedValue private UUID id;
    @Column(name="society_id", nullable=false) private UUID societyId;
    @Column(name="payment_attempt_id", nullable=false) private UUID paymentAttemptId;
    @Column(name="receipt_number", nullable=false) private String receiptNumber;
    @Column(name="receipt_date", nullable=false) private LocalDate receiptDate;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    @Column(name="currency_code", nullable=false) private String currencyCode="INR";
    @Column(name="document_url") private String documentUrl;
    @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
    public UUID getId(){return id;} public String getReceiptNumber(){return receiptNumber;}
    public static Receipt create(UUID society, UUID payment, String number, BigDecimal amount) { Receipt r=new Receipt();r.societyId=society;r.paymentAttemptId=payment;r.receiptNumber=number;r.receiptDate=LocalDate.now(ZoneOffset.UTC);r.amount=amount;return r; }
}
