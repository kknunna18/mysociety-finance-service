# Finance database migrations

`migrations/V1__finance_schema.sql` is the Finance Service's PostgreSQL 16+
schema migration. Apply it manually before starting the service:

```powershell
psql -U mysociety -d mysociety -f database\migrations\V1__finance_schema.sql
```

The migration idempotently creates the `pgcrypto` extension and `mysociety`
schema, then creates only Finance-owned tables: `charge_heads`, `billing_runs`,
`invoices`, `invoice_lines`, `invoice_adjustments`, `payment_attempts`,
`payment_events`, `payment_allocations`, and `receipts`. It preserves the
canonical `NUMERIC` precision, checks, indexes, service-internal foreign keys,
and the `payment_attempts` society-scoped idempotency uniqueness constraint.

`society_id`, `unit_id`, and user UUID columns intentionally have no database
foreign keys because their owners are Society and Identity services. Finance
validates those identifiers through their APIs and subscribed events before
writing Finance records. Do not add cross-service database foreign keys.

No outbox table is included: the canonical schema does not define one, and this
service does not claim ownership of a shared outbox schema. The durable
idempotency record is `payment_attempts`, using `UNIQUE (society_id,
idempotency_key)`.

These scripts are versioned artifacts only; this service intentionally does not
integrate Flyway and retains `spring.jpa.hibernate.ddl-auto=validate`.
