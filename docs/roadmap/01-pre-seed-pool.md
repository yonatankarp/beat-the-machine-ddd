# Pre-seed pool (background producer + instant serve)

Status: implemented (feat/pre-seed-pool, PR #19).
Builds on: PR #18 (local LLM + image generation).

## Problem

Generation is slow (local SD: minutes per image; even paid takes
seconds). Today `StartChallenge` triggers generation and the player
watches a pending picture poll to READY. With real local generation
that wait makes the game unplayable.

## Goal

Always serve an already-READY challenge instantly. A background
producer keeps a pool of pre-generated `(prompt, image)` templates
topped up; `StartChallenge` consumes a ready one instead of
generating on demand. Each generated template is reusable, so it
becomes a seed for future players.

## Key decision (must resolve before building)

Replenish trigger — the app has no auth/identity, so "the user
finished X% of available options" is not measurable server-side:

- RECOMMENDED — global reusable library: keep N ready templates per
  difficulty; refill on a low-watermark (ready count < threshold, or
  K serves since last top-up). Repeats allowed. No identity needed.
- Alternative — per-player novelty: client tracks and sends its
  played-set so it never repeats. Needs client cooperation; more
  surface.

## Scope (assuming the global library)

- New `ChallengeTemplate` concept: `(difficulty, prompt, pictureUrl)`,
  persisted (reuse the `picture` blob table for the image).
- A template store / pool, queried by difficulty for a random ready
  entry.
- Invert `StartChallengeUseCase`: pick a ready template and
  instantiate a fresh `Challenge` (new id, full lives) from it,
  instead of calling `PromptSource` + enqueuing a picture.
- Background producer: reuse `PicturePregeneration`'s scope to run
  `PromptSource` -> `Machine` -> store template, triggered on a
  low-watermark per difficulty.
- Cold start: the existing `SeedData` prompts/images are the initial
  pool contents; the producer grows it.
- Fallback when a difficulty's pool is empty (producer can't keep
  pace): fall back to on-demand generation (today's path) or serve a
  reused template, rather than blocking.

## Prerequisite

Verify at least one image backend renders a prompt end-to-end (sd-cpu
or native MPS) before trusting the producer — see the gap list; no
real image has been generated yet.

## Acceptance

- `StartChallenge` returns a READY picture with no client polling when
  the pool is warm.
- Pool refills automatically without a request in flight.
- Boots and plays with `seed` providers (pool seeded from SeedData).
