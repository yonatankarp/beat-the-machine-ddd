# Beat The Machine

Backend and player UI for [Beat the machine!](https://beat-the-machine.yonatankarp.com/),
a hangman-style game played against AI-generated images: guess the words of a
hidden prompt from the picture before running out of lives.

## Quick start

Requires JDK 25+ (and Node.js 22+ only for UI development). Tasks run through the
Makefile; `make help` lists them all.

```shell
make run-inmemory   # backend at http://localhost:8080 (open it to play the bundled UI)
make ui             # in a second shell: SPA dev server with hot reload at /app
```

`make run-inmemory` serves the full app (the SPA is bundled into the backend, and
`/` redirects to `/app`). For UI work, `make ui` runs Vite with hot reload at
http://localhost:5173/app/ and proxies its `/api` calls to that backend. Use
`make run` for durable SQLite storage instead of in-memory.

`make build` compiles everything including the SPA bundle; `make test` runs the
full suite.

## Architecture

A ports-and-adapters (hexagonal) layout, dependencies pointing inward so the
domain never depends on a framework:

- `beat-the-machine-domain`: the `Challenge` aggregate and its value objects.
  Pure Kotlin, no Spring.
- `beat-the-machine-application`: use cases and the outbound ports.
- `beat-the-machine-adapters`: the Spring Boot app (REST API, persistence, the
  async AI picture pipeline). Also serves the SPA at `/app`.
- `beat-the-machine-frontend`: the React + Vite player UI, built into the backend
  jar at package time.

The HTTP API is defined in
[`docs/openapi/beat-the-machine-openapi.yaml`](docs/openapi/beat-the-machine-openapi.yaml);
both the Kotlin server models and the frontend client are generated from it. The
secret prompt is never serialized while a challenge is `IN_PROGRESS`.

## Contributing

Enable the formatting pre-commit hook once per clone with `make setup`. CI
enforces the same check (ktlint via Spotless); auto-fix with
`./gradlew --init-script gradle/spotless.init.gradle.kts spotlessApply`.
