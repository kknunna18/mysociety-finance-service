# MySociety Finance Service

Spring Boot 3 / Java 21 finance backend on port `8083` with base path `/api/v1`.

## Run

1. Apply the authoritative shared schema from [
   `mysociety_postgresql_complete.sql`](https://raw.githubusercontent.com/kknunna18/identity-service/main/mysociety_postgresql_complete.sql)
   to PostgreSQL 16.
2. Copy `.env.example` into your environment and supply a strong `JWT_HMAC_SECRET`.
3. Run `gradlew.bat clean build` on Windows, then `gradlew.bat bootRun --args="--spring.profiles.active=local"`.

The service uses `mysociety` schema and `ddl-auto=validate`; it never creates or updates the shared database schema.

## APIs

`/charge-heads`, `/billing-runs`, `/invoices`, `/payments`, `/payments/{id}/allocations`,
`/payments/{id}/refunds`, `/payments/{id}/receipt`, `/reconciliation`, and
`/webhooks/{provider}`. OpenAPI is at `/api/v1/swagger-ui/index.html`; liveness/readiness
and Prometheus are exposed through Actuator.

## Security and operations

All finance APIs require an HS256 JWT issued by `mysociety-identity`. The `sub` and
`society_id` claims must be UUIDs; tenant identity is never accepted from request payloads.
Payment initiation requires `Idempotency-Key`, persisted in `payment_attempts` under a
society-scoped unique constraint. Webhooks are denied unless a matching configured provider
HMAC secret validates the signature. Never place tokens, card data, provider secrets, or raw
webhook payloads in logs.

The supplied DDL does not define an outbox table. The `TransactionalOutbox` port emits the
versioned Kafka-compatible event envelope but its default adapter only records safe metadata;
deployments must provide a transactional, database-backed implementation and Kafka publisher
against an approved shared-schema migration before enabling external event delivery.
