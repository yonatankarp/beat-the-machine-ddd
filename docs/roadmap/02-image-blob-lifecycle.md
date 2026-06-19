# Image blob lifecycle (bound DB growth)

Status: draft / scope only — implementation TBD.
Builds on: the pre-seed pool (01).

## Problem

Generated PNGs are stored as blobs in the SQLite `picture` table
(see PR #18). A continuously-growing pool means blobs accumulate
forever, so the database grows unbounded.

## Goal

Keep storage bounded without losing in-use images.

## Scope

- Decide a retention policy. Options:
  - Cap the pool per difficulty (producer stops at N ready
    templates); bounded by construction.
  - Evict orphaned pictures: blobs not referenced by any live
    challenge or pool template.
  - Age-based eviction for least-recently-served templates.
- A cleanup pass (scheduled, or on producer top-up) that deletes
  unreferenced rows from `picture`.
- Ensure deletion never removes a blob a live challenge still points
  at (the picture URL is `/images/{id}`).
- `VACUUM` consideration for SQLite to reclaim space after deletes.

## Acceptance

- DB size stabilises under sustained play + replenishment.
- No live challenge ever 404s on its `/images/{id}` due to cleanup.
