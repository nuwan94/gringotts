# ADR 0001 — Monorepo built with Gradle convention plugins

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

Gringotts has around ten services and a handful of shared libraries owned by one team. Services must
share build conventions (Java version, formatting, test setup, dependency versions) without
copy-paste, and a change to a shared library must be tested against every consumer before it merges.

## Decision

- One Git repository holding all services, platform libraries, contracts and deployment config.
- Gradle (Kotlin DSL) with an included build, `build-logic/`, that holds *convention plugins*:
  - `gringotts.java-conventions`: Java 25 toolchain, `-Werror`, Spotless, JUnit, JaCoCo
  - `gringotts.library-conventions`: shared libraries under `platform/`
  - `gringotts.spring-service-conventions`: deployable Spring Boot services
- Versions live in one place, `gradle/libs.versions.toml`. Spring Boot's BOM manages third-party
  versions; the catalog pins only what the BOM does not.
- The Gradle daemon runs on JDK 25 (`gradle/gradle-daemon-jvm.properties`), auto-provisioned if
  missing, so local and CI builds use the same JVM.

## Consequences

- Atomic cross-service changes and a single CI pipeline; the build graph tells us what to rebuild.
- Services cannot drift onto different framework versions. This is intended, but an upgrade has
  to move every service at once.
- If the repository grows past what one CI run handles comfortably, we add Gradle build caching
  and affected-module detection before considering a split.
