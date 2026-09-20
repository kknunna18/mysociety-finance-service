/*
 * MySociety Finance Service schema
 * PostgreSQL 16+
 *
 * This migration is deliberately limited to Finance-owned tables. UUIDs that
 * identify societies, units, and users are validated by upstream service APIs
 * and events; Finance does not create cross-service database foreign keys.
 */

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS mysociety;
SET search_path TO mysociety, public;

CREATE TABLE IF NOT EXISTS charge_heads (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id        UUID NOT NULL,
    code              VARCHAR(40) NOT NULL,
    name              VARCHAR(120) NOT NULL,
    description       VARCHAR(500),
    calculation_type  VARCHAR(30) NOT NULL DEFAULT 'FLAT'
                      CHECK (calculation_type IN ('FLAT','AREA_BASED','UNIT_TYPE','METERED','MANUAL')),
    default_amount    NUMERIC(19,2) CHECK (default_amount IS NULL OR default_amount >= 0),
    tax_percentage    NUMERIC(7,4) NOT NULL DEFAULT 0 CHECK (tax_percentage BETWEEN 0 AND 100),
    is_recurring      BOOLEAN NOT NULL DEFAULT TRUE,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version           BIGINT NOT NULL DEFAULT 0,
    UNIQUE (society_id, code)
);

CREATE TABLE IF NOT EXISTS billing_runs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id      UUID NOT NULL,
    run_number      VARCHAR(60) NOT NULL,
    billing_period  VARCHAR(20) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    due_date        DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT','CALCULATING','READY','PUBLISHED','FAILED','CANCELLED')),
    total_units     INTEGER NOT NULL DEFAULT 0 CHECK (total_units >= 0),
    total_amount    NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    error_summary   JSONB,
    created_by      UUID,
    published_by    UUID,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_billing_period CHECK (period_end >= period_start),
    CONSTRAINT ck_billing_due_date CHECK (due_date >= period_start),
    UNIQUE (society_id, run_number)
);

CREATE TABLE IF NOT EXISTS invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id          UUID NOT NULL,
    billing_run_id      UUID REFERENCES billing_runs(id) ON DELETE SET NULL,
    unit_id             UUID NOT NULL,
    invoice_number      VARCHAR(80) NOT NULL,
    billing_period      VARCHAR(20) NOT NULL,
    invoice_date        DATE NOT NULL,
    due_date            DATE NOT NULL,
    currency_code       CHAR(3) NOT NULL DEFAULT 'INR',
    subtotal_amount     NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (subtotal_amount >= 0),
    tax_amount          NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    adjustment_amount   NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(19,2) NOT NULL CHECK (total_amount >= 0),
    paid_amount         NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    outstanding_amount  NUMERIC(19,2) NOT NULL CHECK (outstanding_amount >= 0),
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','PUBLISHED','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED','ADJUSTED')),
    notes               VARCHAR(1000),
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_invoice_due_date CHECK (due_date >= invoice_date),
    CONSTRAINT ck_invoice_paid CHECK (paid_amount <= total_amount),
    CONSTRAINT ck_invoice_outstanding CHECK (outstanding_amount = total_amount - paid_amount),
    UNIQUE (society_id, invoice_number)
);

CREATE INDEX IF NOT EXISTS ix_invoices_unit_status_due
    ON invoices (society_id, unit_id, status, due_date);
CREATE INDEX IF NOT EXISTS ix_invoices_overdue
    ON invoices (society_id, due_date)
    WHERE status IN ('PUBLISHED','PARTIALLY_PAID','OVERDUE');

CREATE TABLE IF NOT EXISTS invoice_lines (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id     UUID NOT NULL,
    invoice_id     UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    charge_head_id UUID REFERENCES charge_heads(id) ON DELETE SET NULL,
    line_number    INTEGER NOT NULL CHECK (line_number > 0),
    description    VARCHAR(300) NOT NULL,
    quantity       NUMERIC(19,4) NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_rate      NUMERIC(19,4) NOT NULL CHECK (unit_rate >= 0),
    taxable_amount NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (taxable_amount >= 0),
    tax_percentage NUMERIC(7,4) NOT NULL DEFAULT 0 CHECK (tax_percentage BETWEEN 0 AND 100),
    tax_amount     NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    line_total     NUMERIC(19,2) NOT NULL CHECK (line_total >= 0),
    metadata       JSONB,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (invoice_id, line_number)
);

CREATE TABLE IF NOT EXISTS invoice_adjustments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id      UUID NOT NULL,
    invoice_id      UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    adjustment_no   VARCHAR(80) NOT NULL,
    adjustment_type VARCHAR(20) NOT NULL CHECK (adjustment_type IN ('DEBIT','CREDIT','WAIVER')),
    amount          NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    reason          VARCHAR(1000) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','APPROVED','REJECTED','APPLIED','CANCELLED')),
    requested_by    UUID,
    approved_by     UUID,
    approved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0,
    UNIQUE (society_id, adjustment_no)
);

CREATE TABLE IF NOT EXISTS payment_attempts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id          UUID NOT NULL,
    unit_id             UUID NOT NULL,
    initiated_by        UUID,
    payment_reference   VARCHAR(100) NOT NULL,
    idempotency_key     VARCHAR(150) NOT NULL,
    provider            VARCHAR(30) NOT NULL,
    provider_order_id   VARCHAR(150),
    provider_payment_id VARCHAR(150),
    payment_method      VARCHAR(30),
    currency_code       CHAR(3) NOT NULL DEFAULT 'INR',
    requested_amount    NUMERIC(19,2) NOT NULL CHECK (requested_amount > 0),
    confirmed_amount    NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (confirmed_amount >= 0),
    refunded_amount     NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (refunded_amount >= 0),
    status              VARCHAR(30) NOT NULL DEFAULT 'CREATED'
                        CHECK (status IN ('CREATED','PENDING','AUTHORIZED','CONFIRMED','FAILED','CANCELLED','REFUND_PENDING','PARTIALLY_REFUNDED','REFUNDED','DISPUTED')),
    failure_code        VARCHAR(100),
    failure_message     VARCHAR(500),
    confirmed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_payment_confirmed CHECK (confirmed_amount <= requested_amount),
    CONSTRAINT ck_payment_refunded CHECK (refunded_amount <= confirmed_amount),
    UNIQUE (society_id, payment_reference),
    UNIQUE (society_id, idempotency_key)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payment_provider_payment
    ON payment_attempts (provider, provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_payment_unit_status
    ON payment_attempts (society_id, unit_id, status, created_at DESC);

CREATE TABLE IF NOT EXISTS payment_events (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id         UUID NOT NULL,
    payment_attempt_id UUID REFERENCES payment_attempts(id) ON DELETE SET NULL,
    provider           VARCHAR(30) NOT NULL,
    provider_event_id  VARCHAR(180) NOT NULL,
    event_type         VARCHAR(100) NOT NULL,
    signature_valid    BOOLEAN NOT NULL,
    event_timestamp    TIMESTAMPTZ,
    payload            JSONB NOT NULL,
    processing_status  VARCHAR(20) NOT NULL DEFAULT 'RECEIVED'
                       CHECK (processing_status IN ('RECEIVED','PROCESSED','IGNORED','FAILED')),
    error_message      VARCHAR(1000),
    received_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at       TIMESTAMPTZ,
    UNIQUE (provider, provider_event_id)
);

CREATE TABLE IF NOT EXISTS payment_allocations (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id         UUID NOT NULL,
    payment_attempt_id UUID NOT NULL REFERENCES payment_attempts(id) ON DELETE RESTRICT,
    invoice_id         UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    allocated_amount   NUMERIC(19,2) NOT NULL CHECK (allocated_amount > 0),
    allocated_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reversed_amount    NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (reversed_amount >= 0),
    CONSTRAINT ck_allocation_reversal CHECK (reversed_amount <= allocated_amount),
    UNIQUE (payment_attempt_id, invoice_id)
);

CREATE TABLE IF NOT EXISTS receipts (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    society_id         UUID NOT NULL,
    payment_attempt_id UUID NOT NULL UNIQUE REFERENCES payment_attempts(id) ON DELETE RESTRICT,
    receipt_number     VARCHAR(80) NOT NULL,
    receipt_date       DATE NOT NULL,
    amount             NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    currency_code      CHAR(3) NOT NULL DEFAULT 'INR',
    document_url       VARCHAR(500),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (society_id, receipt_number)
);

COMMIT;
