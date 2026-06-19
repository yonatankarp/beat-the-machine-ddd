# Deploy (ship to beat-the-machine.yonatankarp.com)

Status: draft / scope only — implementation TBD.
Builds on: everything below in the stack.
Only needed if production hosting is a goal (local-only play does not
require this).

## Goal

Publish and run the app so the public site serves the game.

## Scope

- Build + publish the app image (CI job: build `adapters.jar`,
  containerise via the existing Dockerfile, push to a registry).
- Choose the production generation strategy. A prod GPU is a big ask,
  so realistic options:
  - seed/paid only in prod (no local SD), or
  - a separate GPU host the prod app points at via
    `BTM_IMAGE_LOCAL_SD_BASE_URL`.
  Set the matching `SPRING_AI_MODEL_*` + key as documented.
- Persistence in prod: a durable volume for the SQLite DB + image
  blobs; a backup/restore note. (Couples with blob lifecycle, 02.)
- TLS / domain wiring for beat-the-machine.yonatankarp.com.
- Tighten CSP `img-src` to `'self'` once external seed S3 URLs are no
  longer served in prod.

## Open questions

- Where does prod run (VPS, k8s, PaaS)?
- Is real generation wanted in prod, or seed/paid only?

## Acceptance

- A push to main produces a deployable image.
- The public site serves a playable game with the chosen generation
  strategy.
