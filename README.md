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

The default setup uses seed (static) prompts and seed images so the app boots
with no API keys or GPU. To enable live AI generation, keep reading.

### One-command local (Ollama + seed images)

The compose file bundles the app, an Ollama sidecar, and seed images.

```shell
docker compose up
docker compose exec ollama ollama pull llama3.2
```

Open <http://localhost:8080/app/>. The backend serves the SPA and all
generated images on the same origin.

Set `BTM_PROMPT_PROVIDER=ollama` and `SPRING_AI_MODEL_CHAT=ollama`
(see table below) to switch from seed prompts to Ollama-generated ones.

### Real images on Linux / NVIDIA

```shell
docker compose --profile sd up
```

Then set `BTM_IMAGE_PROVIDER=local-sd`. No extra API key needed; the SD
container is included in the `sd` profile.

### Real images without a GPU (CPU, slow)

For hosts with no NVIDIA GPU — including Apple Silicon, where Docker
cannot use the Mac GPU — run the CPU profile:

```shell
BTM_IMAGE_PROVIDER=local-sd docker compose --profile sd-cpu up
```

The `stable-diffusion-cpu` service runs Automatic1111 CPU-only and is
aliased to the `stable-diffusion` hostname, so the app's default
`BTM_IMAGE_LOCAL_SD_BASE_URL` works unchanged. Caveats:

- Generation is slow (minutes per image). On Apple Silicon the image
  runs under amd64 emulation (slower still) — uncomment `platform:
  linux/amd64` on the service. It is meant to feed the background
  pre-seed pool, not interactive play.
- First run downloads a Stable Diffusion 1.5 checkpoint (~4 GB).
- Not runtime-verified in this repo; adjust the image/flags to your
  chosen A1111 build. A battle-tested alternative is
  AbdBarho/stable-diffusion-webui-docker (`--profile auto-cpu`).

Verify the API is up once the container has booted and pulled a model:

```shell
curl -s http://localhost:7860/sdapi/v1/sd-models | head -c 200
```

### Real images on macOS (Automatic1111 native, fast)

Run Automatic1111 locally with `--api` enabled, then point the backend
at it:

```shell
BTM_IMAGE_LOCAL_SD_BASE_URL=http://host.docker.internal:7860 \
  docker compose up
```

Set `BTM_IMAGE_PROVIDER=local-sd` alongside that URL.

### Provider / environment-variable reference

Switching a Spring AI provider requires **two** env vars — one selects
the adapter, the other activates the matching Spring AI auto-config.
Missing the second causes the app to fail at startup.

| Variable | Values | Notes |
|---|---|---|
| `BTM_PROMPT_PROVIDER` | `seed` (default), `ollama`, `openai` | |
| `SPRING_AI_MODEL_CHAT` | `ollama`, `openai` | Required when prompt |
| | | provider is not `seed` |
| `BTM_IMAGE_PROVIDER` | `seed` (default), `local-sd`, `paid` | |
| `SPRING_AI_MODEL_IMAGE` | `openai` | Required when image |
| | | provider is `paid` |
| `SPRING_AI_OLLAMA_BASE_URL` | e.g. `http://ollama:11434` | Ollama words |
| `BTM_IMAGE_LOCAL_SD_BASE_URL` | e.g. `http://...internal:7860` | SD images |
| `SPRING_AI_OPENAI_API_KEY` | your OpenAI key | `openai` words or |
| | | `paid` images |

**Combinations that work:**

- Seed words + seed images (default, no config needed):
  boots keyless.
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
