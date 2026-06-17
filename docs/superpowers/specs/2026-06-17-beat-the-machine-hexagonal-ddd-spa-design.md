# Beat the Machine: Hexagonal + DDD refactor with a React SPA

Date: 2026-06-17
Status: Draft for review

## Purpose

Refactor the existing Spring Boot + Kotlin "Hangman Challenge" from a single
module with a static in-memory store into a hexagonal (ports and adapters)
architecture with a lightweight DDD domain model, replace the broken
server-rendered UI with a polished React SPA served by the backend, and leave
clean seams so two future layers (LLM prompt generation and AI image
generation, both via Spring AI) slot in as adapters without touching the core.

This is a pet and showcase project. The bar is "a credible reference for
hexagonal + DDD in Kotlin, plus a portfolio-grade frontend", not maximum
pattern density. Patterns that do not pay for themselves in this domain are
deliberately left out (see Out of Scope).

## Background

The current code is mislabeled "deprecated": it compiles, builds, and boots. A
prior 12-dimension evaluation found the root problem is that the one extension
point that matters is the wrong shape. Phrase and image data are a hardcoded
literal inside a global Kotlin `object` (`RiddleManager`), addressed by
`id % size`, with no interface, no async boundary, no persistence, no config
home, and masking implemented three times in three disagreeing ways. The game
is also effectively stateless and loses all progress on restart.

This refactor replaces that core while preserving the working game logic.

## Ubiquitous language

The vocabulary is taken from the project itself ("Beat the Machine", "Hangman
Challenge") and the real mechanic: the Machine draws a Picture from a hidden
Prompt; the player makes Guesses to reveal the prompt's words before running out
of Lives.

| Term | Meaning |
|------|---------|
| Challenge | One round. The aggregate root. Holds the picture, the hidden prompt, the player's guesses and remaining lives, and the outcome. |
| Prompt | The secret text the Machine was given to generate the picture. The thing the player is guessing. |
| Picture | The AI-generated image the player sees. Has a generation lifecycle. |
| Guess | A single word submitted by the player. |
| MaskedPrompt | The prompt rendered with unguessed words hidden. The single source of truth for display. |
| Lives | The wrong-guess budget. Cannot go below zero. |
| Machine | The thing that produces pictures from prompts. The antagonist. An outbound port. |
| PromptSource | Where prompts come from. An outbound port. |

Names that are retired: `RiddleManager` (a "Manager", and a static store),
`Riddle`, the `startPrompt` / `prompt` ambiguity, and the DTO-nested
`GuessResponse.GuessResult` enum.

## Architecture

Three Gradle modules. Dependencies point inward only, so the compiler enforces
the dependency rule: the domain cannot import Spring even by accident.

```
:domain       pure Kotlin. Aggregates, value objects, domain services,
              domain exceptions. No Spring, no Jackson, no persistence.

:application  depends on :domain. Use cases (inbound ports) and their
              implementations, plus the outbound port interfaces
              (ChallengeRepository, PromptSource, Machine). No framework.

:adapters     depends on :application. Applies the Spring Boot plugin and
              builds the bootJar. The application class lives at the package
              root so component scanning naturally covers every adapter.
                root pkg                  BeatTheMachineApplication + @Configuration wiring
                adapters/in/web           REST controllers, DTOs, @ControllerAdvice
                adapters/out/persistence  InMemory + SQLite repositories
                adapters/out/ai           SeedPromptSource + seed Machine
              Also serves the built SPA from src/main/resources/static.

frontend/     React + TypeScript + Vite. Built by Gradle and copied into
              :adapters static resources for the bootJar.
```

Bootstrap lives at the root of `:adapters` rather than in its own module, by
preference and prior experience: a standalone bootstrap module makes component
scanning and bootJar placement fiddly, and putting the application class at the
adapters root lets its package scan cover everything beneath it.

## Domain model (`:domain`)

The game becomes stateful so the model has real invariants to protect. This is
what gives the DDD substance rather than decoration.

Aggregate root:

```
Challenge
  id:       ChallengeId          value object, UUID
  prompt:   Prompt               the answer; owns the single tokenization rule
  guesses:  Set<Guess>
  lives:    Lives                value object, cannot go negative
  status:   ChallengeStatus      InProgress | Beaten | Lost
  picture:  Picture              value object with a generation lifecycle
  version:  Long                 for optimistic locking

  behavior
    makeGuess(Guess): GuessOutcome
    forfeit()
  invariants (enforced through the root, never by callers)
    no guess accepted unless status is InProgress
    a correct guess reveals the matching word(s)
    a wrong guess decrements lives; at zero the status becomes Lost
    when every word is revealed the status becomes Beaten
    a duplicate guess is a no-op and costs no life
    forfeit reveals the full prompt and sets status to Lost
```

Value objects (these kill the primitive obsession the evaluation flagged):

- `Prompt`: the secret. Owns tokenization with one whitespace rule, in one
  place, replacing the three divergent masking implementations.
- `Guess`: a normalized guessed word. Trimming and case folding live here.
- `MaskedPrompt`: derived from a `Prompt` and a `Set<Guess>`. Returns a
  `List<MaskedToken>` where each token is `Revealed(word)` or `Hidden`. This is
  what the API serializes and the SPA renders.
- `Lives`: the wrong-guess budget. Construction rejects negatives.
- `Picture`: a generation lifecycle, `Pending | Ready(url) | Failed`. Null url
  until ready.
- `ChallengeId`, `Difficulty` (`Difficulty` is unused now and exists to tune
  LLM prompts later).

Domain exceptions: `ChallengeAlreadyOver`, `InvalidGuess` (and
`ChallengeNotFound`, raised at the application boundary). The web adapter
translates these to HTTP.

No domain service is introduced. Masking is pure and lives in the value
objects, so a `MaskingService` would be ceremony.

No domain events. There is a single aggregate, one bounded context, and no
eventual consistency, so events would have no boundary to cross. They are
reconsidered only when a second aggregate or context appears (for example a
leaderboard reacting to a win). Async picture generation is handled at the
application layer and is not a domain event.

## Application layer (`:application`)

Inbound ports (use cases), each a small interface with one implementation:

- `StartChallenge`: ask `PromptSource` for a prompt, create a `Challenge` with
  its picture set to `Pending`, persist it synchronously, hand the picture
  request to the Machine asynchronously, and return the challenge view.
- `MakeGuess`: load the challenge, apply the guess through the aggregate, save
  synchronously, return the outcome.
- `GetChallenge`: load and return a challenge (used for polling picture state).
- `ForfeitChallenge`: load, forfeit through the aggregate, save, return.

Outbound ports (interfaces the application owns, adapters implement):

- `ChallengeRepository`: `save(Challenge)` and `findById(ChallengeId)`. Honors
  the `version` field for optimistic locking.
- `PromptSource`: `next(Difficulty): Prompt`.
- `Machine`: generates a `Picture` from a `Prompt`, asynchronously.

## Persistence and durability

The clarified goal is durability with no data loss, not asynchronous saving.
Asynchronous game-state persistence works against no-data-loss, and saving a
guess is sub-millisecond, so there is nothing to hide. The split is:

- Game state (guess, lives, status): persisted synchronously on every guess
  through `ChallengeRepository`, durable before the player gets a response.
- Picture generation (seconds, external, failure prone): the only thing made
  asynchronous. The challenge is persisted with the picture `Pending`
  synchronously, generation runs on a background pool, and the result is
  persisted as `Ready` or `Failed`. The intent is durable immediately, so a
  crash mid-generation cannot lose state; on restart, challenges stuck in
  `Pending` are retried.

Concurrency is handled with optimistic locking: the `version` column rejects a
conflicting concurrent guess, and the caller retries against fresh state.

Two repository adapters behind the one port: an in-memory implementation for
development and a SQLite implementation for durability. SQLite keeps the
migration simple while still demonstrating a real Repository whose storage is
an adapter detail. The choice is configuration, not code.

## Adapters (`:adapters`)

Inbound web adapter (`adapters/in/web`): a JSON REST API. Controllers map HTTP
to use-case calls and map use-case results to response DTOs. A
`@ControllerAdvice` maps domain exceptions to HTTP: not found to 404, a guess on
a finished challenge or an optimistic-lock conflict to 409, an invalid guess to
422. Responses carry the masked-prompt tokens, `livesRemaining`, `status`, and
the picture state with its url when ready.

REST surface:

```
POST   /api/challenges                start a new challenge
GET    /api/challenges/{id}           fetch state (also used to poll picture)
POST   /api/challenges/{id}/guesses   submit a guess
POST   /api/challenges/{id}/forfeit   give up
```

Spring forwards unknown non-`/api` paths to `index.html` so client-side routing
works.

Outbound persistence adapter (`adapters/out/persistence`): the in-memory and
SQLite repositories, each mapping the domain aggregate to and from storage and
hiding all persistence detail behind `ChallengeRepository`.

Outbound AI adapter (`adapters/out/ai`): `SeedPromptSource`, which serves the
curated list demoted from the old static store, and a seed Machine that returns
the existing image URLs as `Ready` pictures. The future `LlmPromptSource`
(Spring AI `ChatClient`) and image Machine (Spring AI `ImageModel`) implement
the same two ports and are out of scope here.

## Frontend (`frontend/`, React + TypeScript + Vite)

A single-page app, built by Gradle (`com.github.node-gradle`) and copied into
`:adapters` static resources for the bootJar. Development uses the Vite dev
server with a proxy to the API.

Screens and flow: a start screen, a game screen, and a result state. The game
screen shows the AI Picture, the masked prompt board, a guess input, and an
animated SVG gallows that builds up as lives are lost (the classic hangman
drawing was chosen over a plain counter). Because picture generation is
asynchronous, the game screen shows a loading skeleton while the picture is
`Pending`, polls `GET /api/challenges/{id}` until it is `Ready`, and shows a
placeholder with a retry on `Failed`. A win reveals the full prompt and an
on-theme "you beat the Machine" state; a loss reveals the prompt and the
completed gallows.

The visual bar is a polished showcase. A frontend design skill is invoked
during the build phase for the look and feel rather than specified here.

## Testing strategy

- Domain: pure unit tests, no Spring, fast. Aggregate invariants, `MaskedPrompt`
  across whitespace, punctuation, casing, partial and full reveal, the
  duplicate-guess no-op, and forfeit.
- Application: use-case tests with a fake `ChallengeRepository` and fake
  `PromptSource` and `Machine`.
- Web adapter: `@WebMvcTest` slice tests for status codes, error mapping, and
  response shape.
- Persistence adapter: integration tests against SQLite, including the
  optimistic-lock conflict path.
- AI adapter: the seed implementations tested directly.
- Frontend: component tests with Vitest and React Testing Library.

Behavior is pinned with characterization tests on the current code before any
code moves (phase 0), so the refactor is verifiable against known-good output.
The known `giveUp` bug (it currently marks every word as a miss) is corrected to
reveal the prompt and set `Lost`, and the contradictory test is fixed to assert
the intended behavior first. Coverage must not regress.

## Migration plan (phases)

This is a coupled, mostly sequential refactor, not a parallel fan-out. The
critical path is domain, then application, then adapters, then frontend. The
only real parallelism is at the adapter leaves once the ports are frozen.

0. Characterization tests on current behavior; correct the `giveUp` intent.
1. Multi-module Gradle skeleton; app boots; green build. Fold in the
   evaluation's deploy fixes: drop the dead `stage`/`shadowJar` task and use
   bootJar, align ports on the `PORT` env var, add actuator and `/health`,
   reconcile the JDK and Spring Boot and Kotlin versions, and prune the stale
   and credentialed repositories and publishing block.
2. Domain layer and its unit tests.
3. Application layer and its tests with fakes.
4. Adapters, in parallel once ports are frozen: web REST, persistence (in-memory
   and SQLite with optimistic locking), and the seed AI adapters.
5. Wiring and bootstrap, the asynchronous picture pre-generation with
   retry-on-restart, and an end-to-end green build.
6. React SPA, the Gradle build integration, and serving it as static content. A
   design skill handles the polish.

Peak concurrency is three agents (phase 4). Every phase has a review checkpoint.

## Out of scope

- Domain events, until a real subscriber such as a leaderboard exists.
- The Spring AI LLM and image-generation layers. Their ports (`PromptSource`,
  `Machine`) and the `Pending/Ready/Failed` picture lifecycle are built now so
  they slot in later; the implementations are not.
- Authentication and accounts. The game is single-player and anonymous.
- A multi-challenge "session" wrapper over individual challenges.

## Forward-looking notes for the future AI layers

These are not built now but the design accommodates them. API keys will be
read from the environment through typed `@ConfigurationProperties`, never from
`application.yml` or logs, with a per-source feature flag so the app runs
seed-only when keys are absent. Generated image URLs will be host-allow-listed
before rendering and a Content-Security-Policy header added, since LLM and
image output is untrusted. Provider calls will get explicit timeouts plus
circuit-breaker, retry, and time-limiter behavior with a fallback to the seed
source, and Micrometer instruments for latency, errors, and token and image
cost.
