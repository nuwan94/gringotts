# Contributing to Gringotts

## Workflow: trunk-based, straight to `main`

- All work is committed **directly on `main`** and pushed to `origin/main`. Do not create feature
  branches.
- Keep `main` releasable. Run `./gradlew build` before every push; CI runs the same build on each
  push to `main`.
- Commit small and often. One logical change per commit.
- If CI goes red, fixing it takes priority over new work. Push a fix, or `git revert` the
  offending commit.
- The only branches are the ones Dependabot opens for dependency updates.

## Commit messages: Conventional Commits

Every commit message follows [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/).
The `commit-msg` hook in [.githooks/](.githooks/) enforces it (see [Setup](#setup)).

```
<type>(<scope>): <subject>

[optional body: what and why, wrapped at 72 columns]

[optional footer(s): BREAKING CHANGE: ..., Refs: #123, Co-Authored-By: ...]
```

### Types

| Type | Use for |
|---|---|
| `feat` | A new feature or capability |
| `fix` | A bug fix |
| `docs` | Documentation only (README, ADRs, plan) |
| `style` | Formatting only, no change in behaviour |
| `refactor` | A code change that neither fixes a bug nor adds a feature |
| `perf` | A performance improvement |
| `test` | Adding or correcting tests |
| `build` | Build system, Gradle, dependencies |
| `ci` | GitHub Actions and other CI config |
| `chore` | Maintenance that fits nowhere else |
| `revert` | Reverts an earlier commit |

### Scopes

Optional. Use one when the change sits in a single area:

- Services: `ledger`, `account`, `transfer`, `registry`, `exchange`, `vault`, `fraud`,
  `notification`, `gateway`
- Platform libraries: `money`, `domain`, `web`, `events`, `test`
- Infrastructure: `compose`, `helm`, `k8s`, `argocd`, `keycloak`
- Repository-wide: `deps`, `adr`

### Rules

- The subject is written in the imperative ("add", not "added"), starts lowercase, has no full
  stop, and is at most 72 characters including type and scope.
- The body explains *why*. The diff already shows *what*.
- Mark breaking changes with `!` after the type or scope, and with a `BREAKING CHANGE:` footer.

### Examples

```
feat(ledger): reject journal entries whose postings do not balance
fix(money): handle Long.MIN_VALUE when formatting negative amounts
docs(adr): record decision to use Debezium for outbox relay
build(deps): bump spring-boot to 4.0.9
ci: cache Gradle dependencies between runs
feat(transfer)!: require Idempotency-Key header on POST /v1/transfers

BREAKING CHANGE: requests without the header are now rejected with 400.
```

## Setup

After cloning, enable the repository's git hooks once:

```bash
git config core.hooksPath .githooks
```

## Code style

- `./gradlew spotlessApply` formats Java (google-java-format). CI fails on unformatted code.
- Compiler warnings are errors.
- Record significant decisions as ADRs in [docs/adr/](docs/adr/).
