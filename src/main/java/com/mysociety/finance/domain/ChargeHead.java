package com.mysociety.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "charge_heads", schema = "mysociety")
public class ChargeHead {
    @Id @GeneratedValue private UUID id;
    @Column(name = "society_id", nullable = false) private UUID societyId;
    @Column(nullable = false, length = 40) private String code;
    @Column(nullable = false, length = 120) private String name;
    private String description;
    @Column(name = "calculation_type", nullable = false) private String calculationType = "FLAT";
    @Column(name = "default_amount", precision = 19, scale = 2) private BigDecimal defaultAmount;
    @Column(name = "tax_percentage", nullable = false, precision = 7, scale = 4) private BigDecimal taxPercentage = BigDecimal.ZERO;
    @Column(name = "is_recurring", nullable = false) private boolean recurring = true;
    @Column(name = "is_active", nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @Version private long version;
    public UUID getId() { return id; } public UUID getSocietyId() { return societyId; } public String getCode() { return code; }
    public String getName() { return name; } public BigDecimal getDefaultAmount() { return defaultAmount; } public boolean isActive() { return active; }
    public static ChargeHead create(UUID societyId, String code, String name, BigDecimal amount) { ChargeHead c = new ChargeHead(); c.societyId=societyId; c.code=code; c.name=name; c.defaultAmount=amount; return c; }
    @PreUpdate void updated() { updatedAt = Instant.now(); }
}
