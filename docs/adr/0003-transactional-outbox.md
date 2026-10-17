# ADR 0003 — Publish events through a transactional outbox and Debezium

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

A service that updates its database and then publishes to Kafka can fail between the two steps.
If it does, the event is lost (state changed, nobody told) or invented (event sent, then the
transaction rolls back). A bank cannot accept either outcome.

## Decision

- Services never publish domain events to Kafka directly. They call `OutboxWriter.append(event)`
  from `platform/common-events`, which inserts a row into `outbox_event` **in the same transaction**
  as the state change. The writer refuses to run outside a transaction.
- Debezium (Kafka Connect) reads the Postgres WAL and uses its outbox event router to publish each row
  to `gringotts.<aggregatetype>.events`. The Kafka key is the aggregate ID, so events for one aggregate
  stay in order.
- The event ID, type and correlation ID travel as Kafka headers. Consumers de-duplicate on event ID,
  because delivery is at-least-once.
- Payloads are JSON for now. Avro with Schema Registry arrives in Phase 2, when the first consumers
  exist and compatibility rules start to matter.

## Consequences

- An event is published if and only if its transaction commits.
- Operations must run Kafka Connect and keep Postgres at `wal_level=logical`.
- Every consumer must be idempotent.
