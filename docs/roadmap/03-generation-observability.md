# Generation observability

Status: draft / scope only — implementation TBD.
Builds on: the pre-seed pool (01).

## Problem

Generation runs in the background and can fail silently (a flaky
local SD, a bad LLM phrase, an exhausted pool). Today there is no way
to see whether the pool is filling or starving.

## Goal

Make the producer/pool observable so operators can tell it is
working and diagnose when it is not.

## Scope

- Metrics (Micrometer via the existing actuator):
  - ready pool depth per difficulty (gauge),
  - generations attempted / succeeded / failed (counters),
  - generation latency (timer),
  - on-demand fallback count (signals the pool is starving).
- Structured logs already exist for failures; ensure they carry the
  prompt/difficulty and outcome.
- A lightweight read-only status surface (actuator endpoint or a
  small JSON route) showing pool depth and recent generation
  outcomes.

## Acceptance

- Pool depth and generation success/failure are visible at runtime.
- A starving pool (fallbacks rising, depth at zero) is observable.
