# Local Stable Diffusion Seeding

The local Stable Diffusion defaults live in one checked-in file:

```text
beat-the-machine-adapters/src/main/resources/local-stable-diffusion.yml
```

The backend imports this file at startup, and `make seed-images` reads the same
file before calling the Automatic1111 API. Environment variables still override
the shared defaults for one-off runs.

## Generate Seed Images

Start Automatic1111 with `--api` on port `7860`, then run:

```shell
make seed-images
```

Generate only a subset while tuning prompts:

```shell
BTM_SEED_IMAGE_IDS=seed-easy-01,seed-hard-09 BTM_SEED_IMAGE_FORCE=true make seed-images
```

Use a different shared config file:

```shell
BTM_LOCAL_SD_CONFIG=/path/to/local-stable-diffusion.yml make seed-images
```

Override one value without changing the shared config:

```shell
BTM_IMAGE_LOCAL_SD_STEPS=14 BTM_IMAGE_LOCAL_SD_CFG_SCALE=7.5 make seed-images
```

## Runtime Provider

Use the local Stable Diffusion image adapter with:

```shell
BTM_IMAGE_PROVIDER=local-sd
```

If Automatic1111 is not on `http://localhost:7860`, set:

```shell
BTM_IMAGE_LOCAL_SD_BASE_URL=http://host.docker.internal:7860
```

The backend uses the prompt prefix, prompt suffix, negative prompt, image size,
steps, CFG scale, and timeout from `local-stable-diffusion.yml`.

## Capacity Planning

On Apple Silicon Docker CPU emulation, `384x384` with `10` steps took roughly
two minutes per image. A full 30-image seed run can therefore take about an
hour. Treat local SD as a background pre-generation flow, not a live request
path for demos.

Keep enough challenge templates ready before showing the app. With the current
default pool target of `10` per difficulty, a cold local-SD warmup needs 30
images. Regenerate and review images before they are needed.

## Troubleshooting

- Automatic1111 must be started with `--api`.
- First CPU Docker startup can download a Stable Diffusion 1.5 checkpoint of
  roughly 4 GB.
- On Apple Silicon, the CPU Docker profile runs under amd64 emulation and is
  slow.
- If generated images drift too literal or too abstract, tune
  `prompt-prefix`, `prompt-suffix`, and `negative-prompt` in the shared config,
  then regenerate a small subset before running all 30.
