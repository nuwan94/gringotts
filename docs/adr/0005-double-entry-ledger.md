# ADR 0005 — The ledger is an append-only double-entry journal

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

If balances are columns that every service updates, nobody can explain how a balance came about,
and concurrent updates silently lose money. Banks have solved this problem for centuries.

## Decision

- `ledger-service` is the only source of truth for balances.
- Every movement of money is a **journal entry** made of two or more **postings**. The postings in an
  entry sum to zero per currency. This is enforced in the domain model and by a database constraint.
- Entries are append-only. A mistake is corrected with a reversing entry, never by an `UPDATE` or
  `DELETE`.
- A balance is the sum of an account's postings. It is cached as a projection and can always be
  rebuilt from the journal.
- Holds (reserved funds) are postings to a per-account hold sub-account, so a transfer saga can
  reserve funds and later commit or release them.
- Each entry carries an idempotency key, unique per ledger, so a retried command cannot double-post.

## Consequences

- There is a full audit trail by construction, and reconciliation is a query.
- Other services ask the ledger for balances and never keep their own.
- The ledger is the hottest write path. Its schema and indexes get extra review, and load tests
  in Phase 5 target it first.
