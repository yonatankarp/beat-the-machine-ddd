# Beat The Machine

Backend and player UI for
[Beat the machine!](https://beat-the-machine.yonatankarp.com/),
a hangman-style game played against AI-generated images: guess the words
of a hidden prompt from the picture before running out of lives.

## Quick start

Requires JDK 25+ (and Node.js 22+ only for UI development). Tasks run
through the Makefile; `make help` lists them all.

```shell
make run-inmemory   # backend + bundled UI at http://localhost:8080
make ui             # in a second shell: SPA dev server with hot reload at /app
```

`make run-inmemory` serves the full app: open <http://localhost:8080> and you
are redirected to the UI at `/app`. For UI work, `make ui` runs Vite with hot
reload at <http://localhost:5173/app/> and proxies its `/api` calls to that
backend. Use `make run` for durable SQLite storage instead of in-memory.

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
- `beat-the-machine-frontend`: the player UI, a React SPA built with Vite
  and TypeScript, bundled into the backend jar at package time.

The HTTP API is defined in
[`docs/openapi/beat-the-machine-openapi.yaml`][openapi];
both the Kotlin server models and the frontend client are generated from
it. The secret prompt is never serialized while a challenge is
`IN_PROGRESS`.

[openapi]: docs/openapi/beat-the-machine-openapi.yaml

## Running with real generation

By default the app uses seed (static) prompts and seed images, so it boots
with no API keys or GPU. The bundled catalog contains 30 ready challenge
templates: 10 easy, 10 medium, and 10 hard. Those templates expect bundled
images under `beat-the-machine-adapters/src/main/resources/static/seed/images`.

To generate the bundled seed images from a local Stable Diffusion API, first
start Automatic1111 with `--api` on port 7860, then run:

```shell
make seed-images
```

Use `BTM_IMAGE_LOCAL_SD_BASE_URL=http://host:7860` if your local SD API is not
at `http://localhost:7860`.

To create or refresh that seed pool in the durable SQLite backend, run:

```shell
make seed-challenges
```

The command starts the backend with seed prompts, seed images, and
`BTM_POOL_TARGET=10`. Once the app has started, the template pool is available
from SQLite and new games can be served immediately from ready pictures.
Stop the process with `Ctrl-C` when you are done.

Use `DB_PATH=/path/to/file.db` or `PORT=18080` to override the database file or
HTTP port, for example:

```shell
make seed-challenges DB_PATH=/tmp/beat-the-machine-seed.db PORT=18080
```

The sections below enable live AI generation.

### One-command local (Ollama + seed images)

```shell
docker compose up
docker compose exec ollama ollama pull llama3.2
```

Open <http://localhost:8080/app/>. Set `BTM_PROMPT_PROVIDER=ollama` and
`SPRING_AI_MODEL_CHAT=ollama` (see the table below) to switch from seed
prompts to Ollama-generated ones.

### Real images on Linux / NVIDIA

```shell
docker compose --profile sd up
```

Then set `BTM_IMAGE_PROVIDER=local-sd`. No API key needed; the
Stable Diffusion container is included in the `sd` profile.

### Real images without a GPU (CPU, slow)

For hosts with no NVIDIA GPU — including Apple Silicon, where Docker
cannot use the Mac GPU:

```shell
BTM_IMAGE_PROVIDER=local-sd docker compose --profile sd-cpu up
```

The `stable-diffusion-cpu` service is aliased to the
`stable-diffusion` hostname so the default
`BTM_IMAGE_LOCAL_SD_BASE_URL` works unchanged. Caveats:

- Generation is slow (minutes per image). On Apple Silicon the image
  runs under amd64 emulation — uncomment `platform: linux/amd64` in
  compose.yaml. Meant for the background pre-seed pool, not live play.
- First run downloads a Stable Diffusion 1.5 checkpoint (~4 GB).
- Not runtime-verified in this repo; adjust the image/flags to your
  chosen A1111 build. Alternative:
  AbdBarho/stable-diffusion-webui-docker (`--profile auto-cpu`).

### Real images on macOS (Automatic1111 native)

Run Automatic1111 locally with `--api` enabled, then point the backend
at it:

```shell
BTM_IMAGE_LOCAL_SD_BASE_URL=http://host.docker.internal:7860 \
  BTM_IMAGE_PROVIDER=local-sd \
  docker compose up
```

### Provider / environment-variable reference

Switching a Spring AI provider requires **two** env vars: one selects
the adapter, the other activates the matching Spring AI auto-config.
Missing the second causes a boot failure.

| Variable | Values | Notes |
|---|---|---|
| `BTM_PROMPT_PROVIDER` | `seed` (default), `ollama`, `openai` | |
| `SPRING_AI_MODEL_CHAT` | `ollama`, `openai` | Must match prompt |
| | | provider (not `seed`) |
| `BTM_IMAGE_PROVIDER` | `seed` (default), `local-sd`, `paid` | |
| `SPRING_AI_MODEL_IMAGE` | `openai` | Required for `paid` |
| `SPRING_AI_OLLAMA_BASE_URL` | e.g. `http://ollama:11434` | Ollama |
| `BTM_IMAGE_LOCAL_SD_BASE_URL` | e.g. `http://...internal:7860` | SD |
| `SPRING_AI_OPENAI_API_KEY` | your OpenAI key | `openai` or `paid` |

**Combinations that work:**

- Seed words + seed images (default): boots keyless, no config.
- Ollama words + seed images:
  `BTM_PROMPT_PROVIDER=ollama SPRING_AI_MODEL_CHAT=ollama`
- OpenAI words + seed images:
  `BTM_PROMPT_PROVIDER=openai SPRING_AI_MODEL_CHAT=openai`
  `SPRING_AI_OPENAI_API_KEY=<key>`
- Seed words + local-SD images:
  `BTM_IMAGE_PROVIDER=local-sd`
  `BTM_IMAGE_LOCAL_SD_BASE_URL=http://host.docker.internal:7860`
- Any words + paid (OpenAI DALL-E) images:
  `BTM_IMAGE_PROVIDER=paid SPRING_AI_MODEL_IMAGE=openai`
  `SPRING_AI_OPENAI_API_KEY=<key>`

## Contributing

Enable the formatting pre-commit hook once per clone with `make setup`. CI
enforces the same check (ktlint via Spotless); auto-fix with
`./gradlew --init-script gradle/spotless.init.gradle.kts spotlessApply`.
