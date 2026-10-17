# Gringotts Wizarding Bank

A wizard bank built the way a real bank would build it: Spring Boot microservices, double-entry
ledger, Kafka events through a transactional outbox, Keycloak, and Kubernetes with GitOps.

See [docs/PLAN.md](docs/PLAN.md) for the architecture and roadmap, and [docs/adr/](docs/adr/) for the
decisions behind it. Read [CONTRIBUTING.md](CONTRIBUTING.md) before committing: work goes straight to `main` with Conventional Commit messages.

## Status

**Phase 0: foundations.** Shared libraries, local infrastructure and CI are in place. Services
arrive in Phase 1.

| Module | What it gives services |
|---|---|
| `platform/common-money` | `Money`: exact Galleon/Sickle/Knut and GBP arithmetic |
| `platform/common-domain` | Domain exceptions with stable error codes, the `DomainEvent` interface |
| `platform/common-web` | Correlation IDs and RFC 9457 problem+json errors, auto-configured |
| `platform/common-events` | Transactional outbox writer, plus the `outbox_event` Flyway migration |
| `platform/common-test` | Testcontainers definitions, ArchUnit hexagonal-architecture rules |

## Prerequisites

- **A JDK to launch Gradle.** Any JDK 17 or later works for that. The build itself runs on JDK 25,
  which Gradle downloads automatically if it is missing.
- **Docker** (Docker Desktop on Windows/macOS) for the local infrastructure and Testcontainers tests.
  Without Docker, the database tests are skipped rather than failing.

## Build

```bash
./gradlew build          # format check, compile (-Werror), tests, coverage
./gradlew spotlessApply  # auto-format
```

## Local infrastructure

```bash
docker compose -f deploy/compose/compose.yaml up -d
docker compose -f deploy/compose/compose.yaml --profile observability --profile tools up -d
```

| Component | Address | Notes |
|---|---|---|
| PostgreSQL 17 | `localhost:5432` | One database and role per service, created by `postgres/initdb` |
| Kafka 4 (KRaft) | `localhost:9092` | |
| Schema Registry | http://localhost:8085 | |
| Kafka Connect + Debezium | http://localhost:8083 | Outbox connector template in `deploy/compose/debezium/` |
| Redis | `localhost:6379` | |
| Keycloak | http://localhost:8180 | Realm `gringotts`; test users and roles in `deploy/compose/keycloak/gringotts-realm.json` |
| Mailpit (Owl Post) | http://localhost:8025 | Catches all outgoing mail |
| Grafana LGTM | http://localhost:3000 | `observability` profile; OTLP on 4317/4318 |
| Kafka UI | http://localhost:8090 | `tools` profile |

All credentials in `deploy/compose` are local-development values only.

### Getting a token locally

```bash
curl -s http://localhost:8180/realms/gringotts/protocol/openid-connect/token -d grant_type=password -d client_id=gringotts-dev-cli -d username=harry.potter -d password=<see realm file>
```

## Layout

```
build-logic/       Gradle convention plugins
platform/          shared libraries
services/          microservices (from Phase 1)
deploy/compose/    local infrastructure
docs/              plan and ADRs
.github/           CI, Dependabot
.githooks/         commit-msg hook (Conventional Commits)
```
