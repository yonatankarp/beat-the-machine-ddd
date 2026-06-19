# Beat The Machine

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

The backend also serves the player SPA under `/app` from its static resources,
built by the `:beat-the-machine-frontend` subproject.

### REST API

| Method  | Path                           | Description                       |
|---------|--------------------------------|-----------------------------------|
| `POST`  | `/api/challenges`              | Start a new challenge             |
| `GET`   | `/api/challenges/{id}`         | Fetch the current state           |
| `POST`  | `/api/challenges/{id}/guesses` | Submit a guess (`{"word":"..."}`) |
| `POST`  | `/api/challenges/{id}/forfeit` | Give up; reveals the prompt       |

The secret prompt is never serialized while a challenge is `IN_PROGRESS`.

## Getting Started

These instructions will get you a copy of the project up and running on your
local machine for development and testing purposes.

### Prerequisites

To run the project you need to install the following:

- JDK 25 or newer
- Node.js 22 or newer (only to run the SPA dev server; the production build
  uses a Gradle-managed Node toolchain and needs nothing installed)

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

#### User interface (SPA)

The player-facing UI is a React SPA in the `:beat-the-machine-frontend` module.
In production it is built and bundled into the backend jar, so a running backend
serves it at `/app` (visiting `/` redirects there). No separate process is
needed once the app is built.

For UI development you run the Vite dev server, which gives hot reload and
proxies `/api` calls to the backend. Run the backend and the dev server in two
shells:

```shell
# shell 1: backend on port 8080 (matches the dev-server proxy)
make run-inmemory

# shell 2: the UI dev server with hot reload
make ui
```

Then open `http://localhost:5173/app/` (the `/app` prefix is required; the
SPA's client-side routes live under it). The raw commands behind `make ui` are
`cd beat-the-machine-frontend && npm install && npm run dev`.

To produce the production bundle without starting anything, run `make ui-build`
(or `./gradlew :beat-the-machine-frontend:buildWebApp`). The regular
`./gradlew build` already includes it.

### Coding style

Code style (ktlint via Spotless) is enforced in the CI pipeline rather than the
local build. CI fails on any formatting violation, so format before pushing.

To catch violations before they reach CI, enable the bundled pre-commit hook
once per clone:

```shell
make setup
```

This sets `core.hooksPath` to `.githooks`, whose `pre-commit` runs the same
Spotless check CI runs (via `gradle/spotless.init.gradle.kts`) and blocks the
commit on violations. Auto-fix them with:

```shell
./gradlew --init-script gradle/spotless.init.gradle.kts spotlessApply
```

## Built With

- [OpenJDK 25](https://openjdk.org/projects/jdk/25/)
- [Kotlin](https://kotlinlang.org/)
- [Spring Boot](https://spring.io/projects/spring-boot) - The web framework used
- [SQLite](https://www.sqlite.org/) - Durable persistence
- [Gradle](https://gradle.org/) - Build tool (Kotlin DSL, multi-module)
- [GitHub Actions](https://docs.github.com/en/actions) - Continuous Integration

## Authors

- **Yonatan Karp-Rudin** - *Initial work* - [yonatankarp](https://github.com/yonatankarp)
