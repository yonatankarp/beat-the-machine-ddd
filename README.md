# Beat The Machine!

[ci-badge]: https://github.com/yonatankarp/beat-the-machine-deprecated/actions/workflows/ci.yml/badge.svg
[ci-state]: https://github.com/yonatankarp/beat-the-machine-deprecated/actions/workflows/ci.yml
[quality-badge]: https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_beat-the-machine&metric=alert_status
[quality-state]: https://sonarcloud.io/summary/new_code?id=yonatankarp_beat-the-machine
[maintainability-badge]: https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_beat-the-machine&metric=sqale_rating
[maintainability-state]: https://sonarcloud.io/summary/new_code?id=yonatankarp_beat-the-machine
[tech-debt-badge]: https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_beat-the-machine&metric=sqale_index
[tech-debt-state]: https://sonarcloud.io/summary/new_code?id=yonatankarp_beat-the-machine
[security-badge]: https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_beat-the-machine&metric=security_rating
[security-state]: https://sonarcloud.io/summary/new_code?id=yonatankarp_beat-the-machine
[vulnerabilities-badge]: https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_beat-the-machine&metric=vulnerabilities
[vulnerabilities-state]: https://sonarcloud.io/summary/new_code?id=yonatankarp_beat-the-machine

| **Type**     | **Status**                                                                                                                                                                             |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CI pipelines | [![Build][ci-badge]][ci-state]                                                                                                                                                         |
| Maintenance  | [![Quality Gate Status][quality-badge]][quality-state] [![Maintainability Rating][maintainability-badge]][maintainability-state] [![Technical Debt][tech-debt-badge]][tech-debt-state] |
| Security     | [![Security Rating][security-badge]][security-state] [![Vulnerabilities][vulnerabilities-badge]][vulnerabilities-state]                                                                |

This repository hosts the backend for [Beat the machine!](https://beat-the-machine.yonatankarp.com/),
a "Hangman Challenge" played against AI-generated images.

## Architecture

The backend follows a ports-and-adapters (hexagonal) layout across three Gradle
modules, with dependencies pointing inward so the domain never depends on a
framework:

- `beat-the-machine-domain` — the stateful `Challenge` aggregate and its value
  objects (`Prompt`, `Guess`, `Lives`, `MaskedPrompt`, `Picture`, …). Pure
  Kotlin, no Spring.
- `beat-the-machine-application` — use cases (`StartChallenge`, `MakeGuess`,
  `GetChallenge`, `ForfeitChallenge`) and the outbound ports
  (`ChallengeRepository`, `PromptSource`, `Machine`). Depends only on the domain.
- `beat-the-machine-adapters` — the Spring Boot application: a JSON REST web
  adapter, in-memory and SQLite persistence adapters, seed AI adapters, and the
  asynchronous picture-generation pipeline.

The HTTP API is JSON-only; the player-facing UI is a separate SPA (tracked in a
follow-up plan).

### REST API

| Method | Path                              | Description                          |
|--------|-----------------------------------|--------------------------------------|
| `POST` | `/api/challenges`                 | Start a new challenge                |
| `GET`  | `/api/challenges/{id}`            | Fetch a challenge's current state    |
| `POST` | `/api/challenges/{id}/guesses`    | Submit a guess (`{"word":"..."}`)    |
| `POST` | `/api/challenges/{id}/forfeit`    | Give up; reveals the prompt          |

The secret prompt is never serialized while a challenge is `IN_PROGRESS`.

## Getting Started

These instructions will get you a copy of the project up and running on your
local machine for development and testing purposes.

### Prerequisites

To run the project you need to install the following:

- JDK 25 or newer

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```shell
  ./gradlew build
```

### Running the application

You can run the Spring Boot application directly from Gradle:

```shell
./gradlew :beat-the-machine-adapters:bootRun
```

This starts the API on the port read from `PORT` (defaulting to `80`).

In order to test if the application is up, you can call its health endpoint:

```shell
  curl http://localhost:80/health
```

You should get a response similar to this:

```json
  {"status":"UP"}
```

#### Persistence

The repository implementation is selected by the `btm.persistence` property:

- `sqlite` (default) — durable storage in a SQLite file at `BTM_DB_PATH`
  (defaults to `beat-the-machine.db`).
- `inmemory` — non-durable storage, handy for tests and demos.

```shell
./gradlew :beat-the-machine-adapters:bootRun --args='--btm.persistence=inmemory'
```

### Coding style

Code style (ktlint via Spotless) is enforced in the CI pipeline rather than the
local build. CI fails on any formatting violation, so format before pushing.

## Built With

- [OpenJDK 25](https://openjdk.org/projects/jdk/25/)
- [Kotlin](https://kotlinlang.org/)
- [Spring Boot](https://spring.io/projects/spring-boot) - The web framework used
- [SQLite](https://www.sqlite.org/) - Durable persistence
- [Gradle](https://gradle.org/) - Build tool (Kotlin DSL, multi-module)
- [GitHub Actions](https://docs.github.com/en/actions) - Continuous Integration

## Authors

- **Yonatan Karp-Rudin** - *Initial work* - [yonatankarp](https://github.com/yonatankarp)
