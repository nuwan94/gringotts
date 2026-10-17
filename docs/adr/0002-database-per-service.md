# ADR 0002 — Database per service

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

Shared databases couple services through their schemas: one team's migration can break another
service, and nothing stops a service from reading tables it does not own.

## Decision

- Each service owns one PostgreSQL database and a role that owns it. No service has credentials
  for another service's database.
- Schema changes are Flyway migrations inside the owning service.
- Data needed across services travels through APIs (synchronous) or events (asynchronous), never
  through joins.
- Locally, one Postgres instance hosts every database (`deploy/compose/postgres/initdb`). In
  Kubernetes, each service gets its own database, and production may use separate clusters.

## Consequences

- No cross-service transactions. Multi-service operations such as transfers use sagas
  ([ADR 0003](0003-transactional-outbox.md)).
- Reporting across services needs an event-fed read model or a warehouse, not ad-hoc SQL.
