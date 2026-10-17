# Gringotts Wizarding Bank — Platform Plan

> "Fortune favours the well-architected." A wizard bank built the way a real
> enterprise bank would build it: domain-driven microservices on Spring Boot,
> event-driven with Kafka, deployed to Kubernetes through GitOps, observable,
> secure, and tested end to end.

---

## 1. Business domain

### Currency
Wizarding money is non-decimal, so it is a good forcing function for proper money handling.

| Unit    | Value            |
|---------|------------------|
| Knut    | base unit        |
| Sickle  | 29 Knuts         |
| Galleon | 17 Sickles = 493 Knuts |

**Rule:** every amount is stored and transported as a `long` count of Knuts plus a currency code
(`WZG` for wizarding gold, `GBP` for Muggle pounds in pence). Formatting to `12G 5S 3K` happens only at the edge.
Never floating point.

### Bounded contexts (one service each)

| Service | Responsibility | Lore flavour |
|---|---|---|
| `wizard-registry` | Customers, KYC, Ministry registration number, wand ID, addresses, risk rating | Know Your Wizard |
| `account-service` | Current & savings accounts, account lifecycle (open / freeze / close), limits | |
| `ledger-service` | Double-entry, append-only journal. **The only source of truth for balances.** | The goblins' great book |
| `transfer-service` | Payments between accounts, saga orchestration, idempotency | Owl Post Transfers |
| `exchange-service` | GBP ↔ Galleon rates, quotes with expiry, conversions | Muggle money desk |
| `vault-service` | High-security vaults: physical gold + enchanted items inventory, access requests, cart rides, guard enchantments | Vault 713 |
| `fraud-service` | Real-time rule engine on the transaction stream (velocity, Polyjuice impersonation patterns, unusual vault access) | Thief's Downfall |
| `notification-service` | Email/SMS/"owl" notifications from events | Owl Post |
| `loan-service` *(phase 6)* | Goblin loans, interest, repayment schedules | |
| `api-gateway` | Single entry point, routing, JWT validation, rate limiting | The marble hall |

Identity is **not** a hand-written service: Keycloak (realm `gringotts`) issues OIDC tokens.
Roles: `WIZARD` (customer), `GOBLIN_TELLER`, `GOBLIN_VAULT_KEEPER`, `HEAD_GOBLIN` (admin), plus service-to-service clients.

### Key flows
1. **Open an account** — registry KYC approved → account opened → ledger account created (event-driven).
2. **Transfer** — client calls `POST /transfers` with an `Idempotency-Key` → transfer-service saga:
   reserve funds (ledger) → fraud check → commit both legs → publish `TransferCompleted`; compensate on failure.
3. **Exchange GBP → Galleons** — get quote (valid 60s) → accept → two ledger postings across currency accounts.
4. **Vault visit** — wizard requests access → goblin approves → access logged; fraud-service watches for anomalies.

---

## 2. Technology stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 25 (LTS), virtual threads on |
| Framework | Spring Boot 4.x, Spring Cloud 2025.x |
| Build | Gradle (Kotlin DSL) monorepo with convention plugins, version catalog |
| API contracts | OpenAPI 3.1, contract-first (`openapi-generator`), RFC 9457 problem+json errors, `/v1` versioning |
| Persistence | PostgreSQL 17, **database per service**, Flyway migrations, Spring Data JPA (jOOQ in ledger if needed) |
| Messaging | Apache Kafka (KRaft) + Schema Registry, Avro schemas, **transactional outbox** + Debezium CDC |
| Cache / idempotency | Redis |
| Resilience | Resilience4j (circuit breaker, retry, bulkhead, timeouts) |
| Security | Keycloak OIDC, Spring Security resource servers, method-level authorization, mTLS in mesh |
| Secrets | External Secrets Operator + HashiCorp Vault (yes, a Vault inside Gringotts) |
| Observability | OpenTelemetry → Grafana stack: Prometheus (metrics), Loki (logs), Tempo (traces), Grafana dashboards + alerts; structured JSON logs with trace IDs |
| Containers | Buildpacks / Jib images, distroless base, non-root, signed with cosign, SBOM |
| Kubernetes | kind for local; Helm charts (one shared library chart); HPA, PDB, NetworkPolicies, probes, resource limits |
| Service mesh *(optional)* | Linkerd (mTLS, retries, golden metrics) |
| GitOps / CD | Argo CD, app-of-apps, envs `dev` / `staging` / `prod` overlays |
| CI | GitHub Actions: build, test, Sonar-style quality, OWASP/Trivy scans, image push, chart lint |
| Local dev | Docker Compose for infra; Tilt for inner loop on kind |
| Testing | JUnit 5, AssertJ, Testcontainers, Spring Cloud Contract (consumer-driven contracts), ArchUnit, k6 load tests, chaos with Chaos Mesh |

---

## 3. Architecture

```
                 ┌────────────┐
  Wizards ──────▶│ api-gateway│◀── Keycloak (OIDC)
                 └─────┬──────┘
        REST (sync)    │
   ┌─────────┬─────────┼──────────┬───────────┬───────────┐
   ▼         ▼         ▼          ▼           ▼           ▼
registry  account  transfer   exchange      vault       ledger
   │         │         │          │           │           │
   └─────────┴────┬────┴──────────┴───────────┴───────────┘
                  ▼  outbox → Debezium → Kafka (Avro)
        ┌─────────┴──────────┐
        ▼                    ▼
   fraud-service     notification-service
```

Principles:
- **Sync for queries and commands that need an answer, async events for everything else.**
- No shared databases. No distributed transactions — sagas with explicit compensation.
- Ledger is append-only; balances are projections. Corrections are reversing entries, never updates.
- Every mutating endpoint is idempotent (`Idempotency-Key` header, stored in Redis/Postgres).
- Hexagonal layout inside each service: `api` → `application` → `domain` ← `infrastructure`, enforced by ArchUnit.
- Audit trail: every state change emits an event with actor, correlation ID, and timestamp.

---

## 4. Repository layout

```
gringotts/
├── build-logic/                 # Gradle convention plugins (java, spring-service, testing)
├── gradle/libs.versions.toml    # version catalog
├── platform/
│   ├── common-money/            # Knut-based Money type, formatting
│   ├── common-domain/           # domain exceptions, DomainEvent (framework-free)
│   ├── common-web/              # problem+json, correlation IDs (idempotency filter: phase 2)
│   ├── common-events/           # transactional outbox (Avro schemas: phase 2)
│   └── common-test/             # Testcontainers base classes
├── services/
│   ├── api-gateway/
│   ├── wizard-registry/
│   ├── account-service/
│   ├── ledger-service/
│   ├── transfer-service/
│   ├── exchange-service/
│   ├── vault-service/
│   ├── fraud-service/
│   └── notification-service/
├── contracts/openapi/           # one spec per service
├── deploy/
│   ├── compose/                 # local infra: postgres, kafka, keycloak, redis, grafana stack
│   ├── helm/                    # library chart + per-service charts
│   ├── argocd/                  # app-of-apps, environments
│   └── kind/                    # cluster config, bootstrap script
├── tests/
│   ├── e2e/                     # black-box API tests against a running cluster
│   └── load/                    # k6 scripts
├── docs/
│   ├── adr/                     # Architecture Decision Records
│   └── PLAN.md
└── .github/workflows/
```

---

## 5. Delivery phases

Each phase ends with something runnable and demoable.

### Phase 0 — Foundations
- Gradle monorepo, convention plugins, version catalog, Spotless formatting
- `common-*` libraries, with `Money` (Knut arithmetic) fully unit-tested
- Docker Compose infra: Postgres, Kafka + Schema Registry, Redis, Keycloak (realm import), Grafana stack
- GitHub Actions: build + test on every PR
- ADRs 001–005 (monorepo, DB-per-service, outbox, money representation, ledger design)

**Done when:** `./gradlew build` is green and `docker compose up` starts all infra.

**Status (2026-10-18):** done. Two items moved to Phase 2, where their first consumer lives:
the `Idempotency-Key` filter (transfer-service) and Avro schemas plus Schema Registry serdes (first
Kafka consumers). A `common-domain` module was added so that domain code never depends on web or
messaging libraries.

### Phase 1 — Core banking
- `wizard-registry`, `account-service`, `ledger-service`, `api-gateway`
- Keycloak login, roles enforced at gateway and services
- OpenAPI specs, generated interfaces, problem+json errors
- Testcontainers integration tests per service

**Done when:** a wizard can register, open an account, and see a balance via the gateway.

### Phase 2 — Money movement
- `transfer-service` with saga + compensation, idempotency keys
- Transactional outbox + Debezium → Kafka, Avro schemas
- `notification-service` consuming events (Mailpit for local email)
- Consumer-driven contract tests between services

**Done when:** transfers work, survive a killed service mid-saga, and never double-post.

### Phase 3 — Wizarding features
- `exchange-service` (quotes with TTL, rate feed simulator)
- `vault-service` (inventory, access requests, goblin approval workflow)
- `fraud-service` (Kafka Streams rules, holds suspicious transfers)

### Phase 4 — Kubernetes & GitOps
- Container images (Jib, distroless, non-root), cosign signing, SBOM
- Helm library chart; per-service charts with probes, HPA, PDB, NetworkPolicy, ServiceMonitor
- kind cluster bootstrap script, ingress-nginx, cert-manager, External Secrets + Vault
- Argo CD app-of-apps with `dev`/`staging`/`prod` values
- Tilt for local inner loop

**Done when:** one script brings up a kind cluster and Argo CD deploys the whole bank.

### Phase 5 — Production readiness
- OpenTelemetry tracing across gateway → services → Kafka; Grafana dashboards + SLO alerts
- Security: Trivy/OWASP scans in CI, rate limiting, Linkerd mTLS (optional)
- k6 load tests, Chaos Mesh experiments (kill pods, network delay on ledger)
- Runbooks for top failure modes

### Phase 6 — Extras (optional)
- `loan-service`
- Web front end (React/Next.js "Gringotts Online")
- Statement generation (PDF), scheduled jobs

---

## 6. Prerequisites on this machine

- JDK 25: Gradle auto-provisions it (already done on this machine)
- Docker Desktop (with WSL2 backend): **not installed yet**; needed for Compose and Testcontainers
- `kubectl`, `kind`, `helm`
- Optional: `tilt`, `k6`, `argocd` CLI

Gradle does not need installing; the wrapper is committed.

---

## 7. Open decisions

| # | Decision | Default in this plan |
|---|---|---|
| 1 | Java 25 + Spring Boot 4.x vs Java 21 + Boot 3.5 | Java 25 + Boot 4.x |
| 2 | Gradle vs Maven | Gradle (Kotlin DSL) |
| 3 | Helm vs Kustomize | Helm |
| 4 | Service mesh | Optional Linkerd in phase 5 |
| 5 | Front end | Deferred to phase 6 |
| 6 | Git hosting / CI | GitHub + GitHub Actions |
