# Beat the Machine: Hexagonal Backend Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the single-module, static-store Hangman Challenge into a three-module hexagonal Kotlin application with a stateful DDD `Challenge` aggregate, durable persistence, an async picture pipeline, and a JSON REST API, leaving clean ports for the future Spring AI layers.

**Architecture:** Ports and adapters across three Gradle modules (`:domain`, `:application`, `:adapters`). Dependencies point inward so the compiler forbids the domain from importing Spring. The domain holds a stateful `Challenge` aggregate and its value objects. The application holds use cases (inbound ports) and the `ChallengeRepository`, `PromptSource`, and `Machine` outbound ports. The adapters module holds the Spring Boot app at its package root, a REST web adapter, in-memory and SQLite persistence adapters, and seed AI adapters.

**Tech Stack:** Kotlin 2.4.0, Spring Boot 4.1.0, Java 21 toolchain, Gradle (Kotlin DSL) with `buildSrc` convention plugins, Spring Data JDBC + `org.xerial:sqlite-jdbc` for durable persistence, JUnit 5 + MockK + Spring Boot Test, Spotless for formatting.

## Global Constraints

- Kotlin `2.4.0`, Spring Boot `4.1.0`, Java toolchain `21` (Gradle toolchain and Docker base image aligned). Verify `21` satisfies Spring Boot `4.1.0`'s minimum before committing phase 1; fall back to `17` consistently everywhere if not.
- `:domain` must have zero dependencies on Spring, Jackson, Spring Data, or any framework. Only the Kotlin stdlib and test libraries.
- `:application` depends only on `:domain`. No framework dependencies.
- All new domain types are value objects or the aggregate; no primitive `String`/`Int` fields leak across the domain API where a value object is defined.
- One tokenization rule for prompts: split on the regex `\s+`, in `Prompt` only. No other masking implementation may exist.
- Persistence of game state (guess, lives, status) is synchronous. Only picture generation is asynchronous.
- Coverage must not regress versus the current build. Run the full test suite, not incremental, before each phase-closing commit.
- Spotless runs clean (`./gradlew spotlessApply` then `spotlessCheck`) before every commit.
- Commit messages end with the repository's required `Co-Authored-By` trailer.

---

## File Structure

New module layout (old `beat-the-machine/` single module is dismantled across phases 1 to 5):

```
settings.gradle.kts                      include :domain, :application, :adapters
build.gradle.kts                         root plugins + subproject wiring
buildSrc/                                adapted convention plugins

domain/build.gradle.kts
domain/src/main/kotlin/com/yonatankarp/beatthemachine/domain/
    Challenge.kt                         aggregate root
    ChallengeId.kt  Prompt.kt  Guess.kt  Lives.kt  Difficulty.kt
    MaskedPrompt.kt  (MaskedToken sealed type)
    Picture.kt      (PictureStatus / sealed lifecycle)
    ChallengeStatus.kt
    GuessOutcome.kt
    DomainExceptions.kt                  ChallengeAlreadyOver, InvalidGuess
domain/src/test/kotlin/...               pure unit tests

application/build.gradle.kts
application/src/main/kotlin/com/yonatankarp/beatthemachine/application/
    port/in/  StartChallenge.kt MakeGuess.kt GetChallenge.kt ForfeitChallenge.kt
    port/out/ ChallengeRepository.kt PromptSource.kt Machine.kt
    service/  StartChallengeService.kt MakeGuessService.kt
              GetChallengeService.kt ForfeitChallengeService.kt
    ChallengeNotFound.kt
application/src/test/kotlin/...           use-case tests with fakes

adapters/build.gradle.kts
adapters/src/main/kotlin/com/yonatankarp/beatthemachine/
    BeatTheMachineApplication.kt          @SpringBootApplication at root
    config/  BeanConfig.kt PicturePregeneration.kt
    in/web/  ChallengeController.kt dto/ ApiExceptionHandler.kt SpaForwardingController.kt
    out/persistence/ inmemory/InMemoryChallengeRepository.kt
                     sqlite/  SqliteChallengeRepository.kt ChallengeRow.kt mapping
    out/ai/  SeedPromptSource.kt SeedMachine.kt seed-data.kt
adapters/src/main/resources/   application.yml  schema.sql  static/ (SPA later)
adapters/src/test/kotlin/...   @WebMvcTest, SQLite integration, seed adapter tests
```

---

## Phase 0: Pin current behavior

Goal: lock the existing observable behavior with characterization tests in the
current single module before anything moves, and correct the one known bug
(`giveUp` marks every word a miss) so the intended behavior is the baseline.

### Task 0.1: Characterization tests for masking and guessing

**Files:**
- Test: `beat-the-machine/src/test/kotlin/com/yonatankarp/beatthemachine/characterization/MaskingCharacterizationTest.kt`

**Interfaces:**
- Consumes: existing `RiddleService`, `StringUtils`, `Riddle`, `GuessResponse` as they are today.
- Produces: a documented record of current matching behavior (whitespace, casing, punctuation, partial multi-word match, empty guess) that the refactor must preserve or deliberately change.

- [ ] **Step 1: Write characterization tests capturing today's behavior**

```kotlin
package com.yonatankarp.beatthemachine.characterization

import com.yonatankarp.beatthemachine.services.RiddleService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MaskingCharacterizationTest {
    private val service = RiddleService()

    @Test
    fun `masks all words when no guess matches`() {
        // Capture the ACTUAL current output, not the ideal one.
        // Run once, read the failure, then paste the observed value here.
    }

    @Test
    fun `reveals a single matching word case-insensitively`() {
    }

    @Test
    fun `reveals each occurrence of a repeated matching word`() {
    }

    @Test
    fun `handles a guess with leading and trailing whitespace`() {
    }

    @Test
    fun `handles an empty or whitespace-only guess`() {
    }
}
```

- [ ] **Step 2: Run them to discover actual behavior**

Run: `./gradlew :beat-the-machine:test --tests "*MaskingCharacterizationTest" -i`
Expected: tests fail or are empty; read the actual outputs from the service and fill the assertions with the OBSERVED values so the suite is green against today's code.

- [ ] **Step 3: Fill assertions with observed values, re-run to green**

Run: `./gradlew :beat-the-machine:test --tests "*MaskingCharacterizationTest"`
Expected: PASS. These now describe the contract to carry forward.

- [ ] **Step 4: Commit**

```bash
git add beat-the-machine/src/test/kotlin/com/yonatankarp/beatthemachine/characterization/MaskingCharacterizationTest.kt
git commit -m "test: characterize current masking and guessing behavior"
```

### Task 0.2: Correct the giveUp intent in the legacy test

**Files:**
- Modify: `beat-the-machine/src/test/kotlin/com/yonatankarp/beatthemachine/models/RiddleTest.kt`
- Modify: `beat-the-machine/src/main/kotlin/com/yonatankarp/beatthemachine/models/Riddle.kt:11-14`

**Interfaces:**
- Produces: a corrected `giveUp` semantic (reveal the prompt, treat as a loss) that the new domain `forfeit()` will inherit.

- [ ] **Step 1: Rewrite the contradictory test to assert the intended behavior**

The current test name says "as hit" but asserts MISS. Assert that giving up
reveals every word (the answer is shown to the player):

```kotlin
@Test
fun `giveUp reveals every word of the prompt`() {
    val riddle = Riddle(id = 0, startPrompt = "a b", prompt = "hello world", url = "")
    val revealed = riddle.giveUp()
    assertEquals(listOf("hello", "world"), revealed.map { it.first })
}
```

- [ ] **Step 2: Run to confirm it fails against the current bug**

Run: `./gradlew :beat-the-machine:test --tests "*RiddleTest"`
Expected: FAIL (current `giveUp` maps to MISS pairs but the words themselves are still present, so adapt the assertion to whichever property actually encodes reveal-vs-hide; the point is the test now asserts "revealed", not "miss").

- [ ] **Step 3: Note the correct behavior for the domain**

No production change in the legacy module is required here beyond what keeps the
suite honest; the corrected semantic is implemented properly in Phase 2's
`Challenge.forfeit()`. Leave a comment in the test pointing at that task.

- [ ] **Step 4: Commit**

```bash
git add beat-the-machine/src/test/kotlin/com/yonatankarp/beatthemachine/models/RiddleTest.kt
git commit -m "test: assert giveUp reveals the prompt (corrects contradictory test)"
```

---

## Phase 1: Multi-module skeleton and deploy fixes

Goal: stand up `:domain`, `:application`, `:adapters`; move the existing app
into `:adapters` so it still boots and serves; make the build green; fold in the
evaluation's deploy fixes. No behavior change yet.

### Task 1.1: Create the three-module Gradle skeleton

**Files:**
- Modify: `settings.gradle.kts`
- Create: `domain/build.gradle.kts`, `application/build.gradle.kts`, `adapters/build.gradle.kts`
- Modify: `build.gradle.kts`, `buildSrc/src/main/groovy/*` as needed

**Interfaces:**
- Produces: three resolvable modules with the dependency rule wired (`:application` depends on `:domain`; `:adapters` depends on `:application`).

- [ ] **Step 1: Write a test that asserts the module graph**

Add an architecture test in `:adapters` that will later be fleshed out; for now
a simple Gradle check is the test. Create `domain/build.gradle.kts`:

```kotlin
plugins { kotlin("jvm") }
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.11")
}
```

`application/build.gradle.kts`:

```kotlin
plugins { kotlin("jvm") }
dependencies {
    implementation(project(":domain"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.11")
}
```

`adapters/build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}
dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf") // until SPA replaces it
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

- [ ] **Step 2: Wire settings and verify resolution**

`settings.gradle.kts` includes `:domain`, `:application`, `:adapters`.
Run: `./gradlew projects`
Expected: the three modules listed; no `:beat-the-machine` after migration completes (it may temporarily coexist).

- [ ] **Step 3: Move the existing source into `:adapters`**

Move `beat-the-machine/src/main/...` into `adapters/src/main/...` (keeping
packages for now) and `beat-the-machine/src/test/...` into `adapters/src/test`.
Keep the characterization tests compiling.

- [ ] **Step 4: Build green**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL; all existing tests pass under the new module.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "build: split into domain, application, adapters modules"
```

### Task 1.2: Deploy and packaging fixes

**Files:**
- Modify: `adapters/build.gradle.kts`, `adapters/Dockerfile` (moved from `beat-the-machine/Dockerfile`), `Procfile`, `adapters/src/main/resources/application.yml`
- Delete: dead `stage`/`shadowJar` task config, stale Confluent / GitHub Packages repos, `mavenLocal()`, the `sumup/ai-guess-game-poc` publishing block

**Interfaces:**
- Produces: a single `bootJar` deploy path, a working `/health`, port alignment on `PORT`.

- [ ] **Step 1: Add an actuator health smoke test**

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointTest(@Autowired val rest: TestRestTemplate) {
    @Test
    fun `health endpoint reports UP`() {
        val body = rest.getForObject("/actuator/health", String::class.java)
        assertTrue(body.contains("\"status\":\"UP\""))
    }
}
```

- [ ] **Step 2: Run, expect fail, then enable actuator health**

Run: `./gradlew :adapters:test --tests "*HealthEndpointTest"`
Expected: FAIL until actuator is on the classpath and exposed. In
`application.yml` set `management.endpoints.web.exposure.include: health` and map
`/health` (README promises `/health`; either expose actuator at `/health` via
`management.endpoints.web.base-path: /` or document `/actuator/health`).

- [ ] **Step 3: Remove dead packaging and align ports**

Delete the `stage`/`shadowJar` block. Update the Dockerfile to copy and run the
`bootJar` artifact and `EXPOSE` the same port the app reads from `PORT`. Remove
`SERVER_PORT` divergence; standardize on `server.port: ${PORT:80}`. Prune the
stale repositories, `mavenLocal()`, and the publishing block. Set the Java
toolchain and Docker base image to 21 (or 17, consistently).

- [ ] **Step 4: Build, test, and verify boot**

Run: `./gradlew clean build && ./gradlew :adapters:bootRun &` then
`curl -fsS localhost:80/health` (or `/actuator/health`).
Expected: build green, health returns UP.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "build: single bootJar deploy path, working health, aligned ports"
```

---

## Phase 2: Domain layer

Goal: build the stateful `Challenge` aggregate and value objects in `:domain`
with pure unit tests. No framework. This is the heart of the model.

### Task 2.1: Value objects for prompt, guess, masking

**Files:**
- Create: `domain/src/main/kotlin/com/yonatankarp/beatthemachine/domain/Prompt.kt`, `Guess.kt`, `MaskedPrompt.kt`
- Test: `domain/src/test/kotlin/com/yonatankarp/beatthemachine/domain/MaskedPromptTest.kt`, `PromptTest.kt`

**Interfaces:**
- Produces:
  - `@JvmInline value class Guess(val word: String)` with a `normalized()` returning trimmed, lower-cased text; rejects blank.
  - `class Prompt(val text: String)` with `fun words(): List<String>` splitting on `\s+`, rejecting blank.
  - `sealed interface MaskedToken { data class Revealed(val word: String); data object Hidden }`
  - `class MaskedPrompt private constructor(val tokens: List<MaskedToken>)` with `companion object { fun of(prompt: Prompt, guesses: Set<Guess>): MaskedPrompt }` and `fun isFullyRevealed(): Boolean`.

- [ ] **Step 1: Write failing tests for masking**

```kotlin
package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MaskedPromptTest {
    private fun prompt(t: String) = Prompt(t)

    @Test
    fun `hides every word when there are no guesses`() {
        val masked = MaskedPrompt.of(prompt("hello world"), emptySet())
        assertEquals(listOf(MaskedToken.Hidden, MaskedToken.Hidden), masked.tokens)
        assertFalse(masked.isFullyRevealed())
    }

    @Test
    fun `reveals a matching word case-insensitively`() {
        val masked = MaskedPrompt.of(prompt("Hello World"), setOf(Guess("hello")))
        assertEquals(MaskedToken.Revealed("Hello"), masked.tokens[0])
        assertEquals(MaskedToken.Hidden, masked.tokens[1])
    }

    @Test
    fun `reveals every occurrence of a repeated word`() {
        val masked = MaskedPrompt.of(prompt("na na batman"), setOf(Guess("na")))
        assertEquals(
            listOf(MaskedToken.Revealed("na"), MaskedToken.Revealed("na"), MaskedToken.Hidden),
            masked.tokens,
        )
    }

    @Test
    fun `collapses arbitrary whitespace using one rule`() {
        val masked = MaskedPrompt.of(prompt("hello\t \nworld"), setOf(Guess("world")))
        assertEquals(2, masked.tokens.size)
        assertEquals(MaskedToken.Revealed("world"), masked.tokens[1])
    }

    @Test
    fun `is fully revealed when all words are guessed`() {
        val masked = MaskedPrompt.of(prompt("hello world"), setOf(Guess("hello"), Guess("world")))
        assertTrue(masked.isFullyRevealed())
    }
}
```

- [ ] **Step 2: Run, expect compile failure / fail**

Run: `./gradlew :domain:test --tests "*MaskedPromptTest"`
Expected: FAIL (types undefined).

- [ ] **Step 3: Implement the value objects**

```kotlin
// Guess.kt
package com.yonatankarp.beatthemachine.domain

@JvmInline
value class Guess(val word: String) {
    init { require(word.isNotBlank()) { "guess must not be blank" } }
    fun normalized(): String = word.trim().lowercase()
}

// Prompt.kt
package com.yonatankarp.beatthemachine.domain

class Prompt(val text: String) {
    init { require(text.isNotBlank()) { "prompt must not be blank" } }
    fun words(): List<String> = text.trim().split(WHITESPACE)
    companion object { private val WHITESPACE = Regex("\\s+") }
}

// MaskedPrompt.kt
package com.yonatankarp.beatthemachine.domain

sealed interface MaskedToken {
    data class Revealed(val word: String) : MaskedToken
    data object Hidden : MaskedToken
}

class MaskedPrompt private constructor(val tokens: List<MaskedToken>) {
    fun isFullyRevealed(): Boolean = tokens.all { it is MaskedToken.Revealed }
    companion object {
        fun of(prompt: Prompt, guesses: Set<Guess>): MaskedPrompt {
            val guessed = guesses.map { it.normalized() }.toSet()
            return MaskedPrompt(
                prompt.words().map { word ->
                    if (word.lowercase() in guessed) MaskedToken.Revealed(word)
                    else MaskedToken.Hidden
                },
            )
        }
    }
}
```

- [ ] **Step 4: Run to green**

Run: `./gradlew :domain:test --tests "*MaskedPromptTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/
git commit -m "feat(domain): Prompt, Guess, and single-rule MaskedPrompt"
```

### Task 2.2: Lives, ChallengeId, Difficulty, Picture, status, outcome

**Files:**
- Create: `Lives.kt`, `ChallengeId.kt`, `Difficulty.kt`, `Picture.kt`, `ChallengeStatus.kt`, `GuessOutcome.kt`
- Test: `domain/src/test/kotlin/.../LivesTest.kt`, `PictureTest.kt`

**Interfaces:**
- Produces:
  - `@JvmInline value class Lives(val remaining: Int)` rejecting negatives; `fun lose(): Lives` (floors at 0); `fun isExhausted(): Boolean`.
  - `@JvmInline value class ChallengeId(val value: UUID)` with `companion object { fun new(): ChallengeId }`.
  - `enum class Difficulty { EASY, MEDIUM, HARD }`.
  - `sealed interface Picture { data object Pending; data class Ready(val url: String); data object Failed }`.
  - `enum class ChallengeStatus { IN_PROGRESS, BEATEN, LOST }`.
  - `enum class GuessOutcome { HIT, MISS, DUPLICATE, REJECTED }`.

- [ ] **Step 1: Failing tests for Lives and Picture**

```kotlin
class LivesTest {
    @Test fun `cannot be negative`() {
        assertFailsWith<IllegalArgumentException> { Lives(-1) }
    }
    @Test fun `lose decrements and floors at zero`() {
        assertEquals(Lives(0), Lives(1).lose())
        assertEquals(Lives(0), Lives(0).lose())
    }
    @Test fun `is exhausted at zero`() {
        assertTrue(Lives(0).isExhausted())
        assertFalse(Lives(1).isExhausted())
    }
}
```

- [ ] **Step 2: Run, expect fail**

Run: `./gradlew :domain:test --tests "*LivesTest"` → FAIL.

- [ ] **Step 3: Implement the value objects**

```kotlin
@JvmInline
value class Lives(val remaining: Int) {
    init { require(remaining >= 0) { "lives cannot be negative" } }
    fun lose(): Lives = Lives(maxOf(0, remaining - 1))
    fun isExhausted(): Boolean = remaining == 0
}
```

(Implement `ChallengeId`, `Difficulty`, `Picture`, `ChallengeStatus`,
`GuessOutcome` from the Interfaces block above, each in its own file.)

- [ ] **Step 4: Run to green**

Run: `./gradlew :domain:test --tests "*LivesTest" --tests "*PictureTest"` → PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/
git commit -m "feat(domain): Lives, ChallengeId, Difficulty, Picture, status types"
```

### Task 2.3: The Challenge aggregate

**Files:**
- Create: `Challenge.kt`, `DomainExceptions.kt`
- Test: `domain/src/test/kotlin/.../ChallengeTest.kt`

**Interfaces:**
- Consumes: every value object from 2.1 and 2.2.
- Produces:
  - `class Challenge` with read-only accessors `id`, `prompt` (secret, not exposed by the API), `guesses: Set<Guess>`, `lives: Lives`, `status: ChallengeStatus`, `picture: Picture`, `version: Long`, and `fun maskedPrompt(): MaskedPrompt`.
  - `fun makeGuess(guess: Guess): GuessOutcome` enforcing all invariants.
  - `fun forfeit()` revealing the prompt and setting `LOST`.
  - `fun withPicture(picture: Picture): Challenge` (used by the async pipeline; returns a copy with bumped version).
  - `companion object { fun start(prompt: Prompt, lives: Lives, picture: Picture = Picture.Pending, difficulty: Difficulty = Difficulty.MEDIUM): Challenge }`.
  - `class ChallengeAlreadyOver(id: ChallengeId) : RuntimeException`.

- [ ] **Step 1: Failing tests for the aggregate invariants**

```kotlin
class ChallengeTest {
    private fun newChallenge(prompt: String = "hello world", lives: Int = 3) =
        Challenge.start(Prompt(prompt), Lives(lives))

    @Test fun `a correct guess reveals the word and stays in progress`() {
        val c = newChallenge()
        val outcome = c.makeGuess(Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.IN_PROGRESS, c.status)
        assertEquals(3, c.lives.remaining)
    }

    @Test fun `a wrong guess costs a life`() {
        val c = newChallenge()
        val outcome = c.makeGuess(Guess("nope"))
        assertEquals(GuessOutcome.MISS, outcome)
        assertEquals(2, c.lives.remaining)
    }

    @Test fun `guessing every word beats the machine`() {
        val c = newChallenge()
        c.makeGuess(Guess("hello"))
        val outcome = c.makeGuess(Guess("world"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.BEATEN, c.status)
    }

    @Test fun `running out of lives loses the challenge`() {
        val c = newChallenge(lives = 1)
        c.makeGuess(Guess("nope"))
        assertEquals(ChallengeStatus.LOST, c.status)
    }

    @Test fun `a duplicate guess is a no-op and costs no life`() {
        val c = newChallenge()
        c.makeGuess(Guess("nope"))
        val outcome = c.makeGuess(Guess("Nope"))
        assertEquals(GuessOutcome.DUPLICATE, outcome)
        assertEquals(2, c.lives.remaining)
    }

    @Test fun `guessing after the challenge is over is rejected`() {
        val c = newChallenge(lives = 1)
        c.makeGuess(Guess("nope")) // lost
        assertFailsWith<ChallengeAlreadyOver> { c.makeGuess(Guess("hello")) }
    }

    @Test fun `forfeit reveals the prompt and loses`() {
        val c = newChallenge()
        c.forfeit()
        assertEquals(ChallengeStatus.LOST, c.status)
        assertTrue(c.maskedPrompt().tokens.all { it is MaskedToken.Revealed })
    }
}
```

- [ ] **Step 2: Run, expect fail**

Run: `./gradlew :domain:test --tests "*ChallengeTest"` → FAIL.

- [ ] **Step 3: Implement the aggregate**

```kotlin
package com.yonatankarp.beatthemachine.domain

class ChallengeAlreadyOver(val id: ChallengeId) :
    RuntimeException("challenge $id is already over")

class Challenge private constructor(
    val id: ChallengeId,
    private val prompt: Prompt,
    guesses: Set<Guess>,
    lives: Lives,
    status: ChallengeStatus,
    picture: Picture,
    val difficulty: Difficulty,
    val version: Long,
) {
    private val _guesses = guesses.toMutableSet()
    val guesses: Set<Guess> get() = _guesses.toSet()
    var lives: Lives = lives; private set
    var status: ChallengeStatus = status; private set
    var picture: Picture = picture; private set

    fun maskedPrompt(): MaskedPrompt =
        if (status == ChallengeStatus.LOST) MaskedPrompt.of(prompt, prompt.allWordsAsGuesses())
        else MaskedPrompt.of(prompt, _guesses)

    fun makeGuess(guess: Guess): GuessOutcome {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        val normalized = guess.normalized()
        if (_guesses.any { it.normalized() == normalized }) return GuessOutcome.DUPLICATE
        _guesses.add(guess)
        val hit = prompt.words().any { it.lowercase() == normalized }
        return if (hit) {
            if (MaskedPrompt.of(prompt, _guesses).isFullyRevealed()) status = ChallengeStatus.BEATEN
            GuessOutcome.HIT
        } else {
            lives = lives.lose()
            if (lives.isExhausted()) status = ChallengeStatus.LOST
            GuessOutcome.MISS
        }
    }

    fun forfeit() {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        status = ChallengeStatus.LOST
    }

    fun withPicture(picture: Picture): Challenge {
        this.picture = picture
        return this
    }

    // Exposed only to persistence mapping in the adapter module.
    fun secretPrompt(): Prompt = prompt

    companion object {
        fun start(
            prompt: Prompt,
            lives: Lives,
            picture: Picture = Picture.Pending,
            difficulty: Difficulty = Difficulty.MEDIUM,
        ): Challenge = Challenge(
            ChallengeId.new(), prompt, emptySet(), lives,
            ChallengeStatus.IN_PROGRESS, picture, difficulty, version = 0,
        )

        fun rehydrate(
            id: ChallengeId, prompt: Prompt, guesses: Set<Guess>, lives: Lives,
            status: ChallengeStatus, picture: Picture, difficulty: Difficulty, version: Long,
        ): Challenge = Challenge(id, prompt, guesses, lives, status, picture, difficulty, version)
    }
}

private fun Prompt.allWordsAsGuesses(): Set<Guess> =
    words().map { Guess(it) }.toSet()
```

Note: `maskedPrompt()` reveals everything when `LOST` so a forfeit or a loss
shows the answer (this is the corrected `giveUp` behavior from Phase 0).

- [ ] **Step 4: Run to green, then the full domain suite**

Run: `./gradlew :domain:test` → PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/
git commit -m "feat(domain): stateful Challenge aggregate with enforced invariants"
```

---

## Phase 3: Application layer

Goal: use cases (inbound ports) and outbound ports, with tests using fakes. No
framework. Depends only on `:domain`.

### Task 3.1: Outbound ports

**Files:**
- Create: `application/src/main/kotlin/.../port/out/ChallengeRepository.kt`, `PromptSource.kt`, `Machine.kt`

**Interfaces:**
- Produces:
  - `interface ChallengeRepository { fun save(challenge: Challenge): Challenge; fun findById(id: ChallengeId): Challenge? }` where `save` performs an optimistic-locking check on `version` and throws `OptimisticLockConflict` on mismatch.
  - `interface PromptSource { fun next(difficulty: Difficulty): Prompt }`.
  - `interface Machine { fun generate(prompt: Prompt): Picture }` (called from the async pipeline).
  - `class OptimisticLockConflict(id: ChallengeId) : RuntimeException`.

- [ ] **Step 1: Define the ports (interfaces, no test needed for declarations)**

Write the three interfaces and the exception exactly as in the Interfaces block.

- [ ] **Step 2: Compile**

Run: `./gradlew :application:compileKotlin` → SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add application/
git commit -m "feat(application): ChallengeRepository, PromptSource, Machine ports"
```

### Task 3.2: StartChallenge use case

**Files:**
- Create: `application/src/main/kotlin/.../port/in/StartChallenge.kt`, `service/StartChallengeService.kt`
- Test: `application/src/test/kotlin/.../StartChallengeServiceTest.kt`

**Interfaces:**
- Consumes: `PromptSource`, `ChallengeRepository`, `Machine`, `Challenge`.
- Produces:
  - `interface StartChallenge { fun start(difficulty: Difficulty): Challenge }`.
  - `class StartChallengeService(promptSource, repository, pictureRequests: (ChallengeId) -> Unit) : StartChallenge` where `pictureRequests` enqueues async picture generation (default wired in the adapter). It persists the challenge with `Picture.Pending` synchronously, then enqueues generation, then returns.

- [ ] **Step 1: Failing test with fakes**

```kotlin
class StartChallengeServiceTest {
    private val prompts = object : PromptSource {
        override fun next(difficulty: Difficulty) = Prompt("hello world")
    }
    private val saved = mutableListOf<Challenge>()
    private val repo = object : ChallengeRepository {
        override fun save(challenge: Challenge) = challenge.also { saved.add(it) }
        override fun findById(id: ChallengeId) = saved.firstOrNull { it.id == id }
    }

    @Test fun `starts a pending challenge and enqueues picture generation`() {
        val enqueued = mutableListOf<ChallengeId>()
        val service = StartChallengeService(prompts, repo) { enqueued.add(it) }
        val challenge = service.start(Difficulty.MEDIUM)
        assertEquals(Picture.Pending, challenge.picture)
        assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
        assertEquals(listOf(challenge.id), enqueued)
        assertTrue(saved.contains(challenge))
    }
}
```

- [ ] **Step 2: Run, expect fail** → `./gradlew :application:test --tests "*StartChallengeServiceTest"` FAIL.

- [ ] **Step 3: Implement**

```kotlin
interface StartChallenge { fun start(difficulty: Difficulty): Challenge }

class StartChallengeService(
    private val promptSource: PromptSource,
    private val repository: ChallengeRepository,
    private val enqueuePicture: (ChallengeId) -> Unit,
) : StartChallenge {
    private val startingLives = Lives(6)
    override fun start(difficulty: Difficulty): Challenge {
        val challenge = Challenge.start(promptSource.next(difficulty), startingLives)
        val persisted = repository.save(challenge)
        enqueuePicture(persisted.id)
        return persisted
    }
}
```

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Commit**

```bash
git add application/
git commit -m "feat(application): StartChallenge use case"
```

### Task 3.3: MakeGuess, GetChallenge, ForfeitChallenge use cases

**Files:**
- Create: `port/in/MakeGuess.kt` `GetChallenge.kt` `ForfeitChallenge.kt`, `service/MakeGuessService.kt` `GetChallengeService.kt` `ForfeitChallengeService.kt`, `ChallengeNotFound.kt`
- Test: `application/src/test/kotlin/.../MakeGuessServiceTest.kt`, `ForfeitChallengeServiceTest.kt`

**Interfaces:**
- Produces:
  - `class ChallengeNotFound(id: ChallengeId) : RuntimeException`.
  - `interface MakeGuess { fun guess(id: ChallengeId, guess: Guess): Pair<Challenge, GuessOutcome> }`. Loads (or throws `ChallengeNotFound`), mutates, saves synchronously, returns.
  - `interface GetChallenge { fun get(id: ChallengeId): Challenge }`.
  - `interface ForfeitChallenge { fun forfeit(id: ChallengeId): Challenge }`.

- [ ] **Step 1: Failing tests for MakeGuess (hit, miss, not-found, save)**

```kotlin
class MakeGuessServiceTest {
    private val store = linkedMapOf<ChallengeId, Challenge>()
    private val repo = object : ChallengeRepository {
        override fun save(challenge: Challenge) = challenge.also { store[it.id] = it }
        override fun findById(id: ChallengeId) = store[id]
    }
    private fun seed(): Challenge =
        Challenge.start(Prompt("hello world"), Lives(3)).also { store[it.id] = it }

    @Test fun `a hit is persisted`() {
        val c = seed()
        val service = MakeGuessService(repo)
        val (updated, outcome) = service.guess(c.id, Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(MaskedToken.Revealed("hello"), updated.maskedPrompt().tokens[0])
    }

    @Test fun `an unknown challenge throws ChallengeNotFound`() {
        val service = MakeGuessService(repo)
        assertFailsWith<ChallengeNotFound> {
            service.guess(ChallengeId.new(), Guess("hello"))
        }
    }
}
```

- [ ] **Step 2: Run, expect fail** → FAIL.

- [ ] **Step 3: Implement the three services**

```kotlin
class MakeGuessService(private val repository: ChallengeRepository) : MakeGuess {
    override fun guess(id: ChallengeId, guess: Guess): Pair<Challenge, GuessOutcome> {
        val challenge = repository.findById(id) ?: throw ChallengeNotFound(id)
        val outcome = challenge.makeGuess(guess)
        return repository.save(challenge) to outcome
    }
}

class GetChallengeService(private val repository: ChallengeRepository) : GetChallenge {
    override fun get(id: ChallengeId): Challenge =
        repository.findById(id) ?: throw ChallengeNotFound(id)
}

class ForfeitChallengeService(private val repository: ChallengeRepository) : ForfeitChallenge {
    override fun forfeit(id: ChallengeId): Challenge {
        val challenge = repository.findById(id) ?: throw ChallengeNotFound(id)
        challenge.forfeit()
        return repository.save(challenge)
    }
}
```

- [ ] **Step 4: Run to green, full application suite** → `./gradlew :application:test` PASS.

- [ ] **Step 5: Commit**

```bash
git add application/
git commit -m "feat(application): MakeGuess, GetChallenge, ForfeitChallenge use cases"
```

---

## Phase 4: Adapters (parallelizable once ports are frozen)

The three tasks below are independent and may run concurrently. Each implements
ports defined in Phase 3.

### Task 4.1: Seed AI adapters (out/ai)

**Files:**
- Create: `adapters/src/main/kotlin/.../out/ai/SeedPromptSource.kt`, `SeedMachine.kt`, `seed-data.kt`
- Test: `adapters/src/test/kotlin/.../SeedPromptSourceTest.kt`

**Interfaces:**
- Consumes: `PromptSource`, `Machine`.
- Produces: `@Component class SeedPromptSource : PromptSource` returning a randomly-selected curated prompt (the data demoted from the old `RiddleManager`); `@Component class SeedMachine : Machine` returning `Picture.Ready(url)` from the curated image URL paired with the prompt.

- [ ] **Step 1: Failing test**

```kotlin
class SeedPromptSourceTest {
    @Test fun `returns a non-blank curated prompt`() {
        val prompt = SeedPromptSource().next(Difficulty.MEDIUM)
        assertTrue(prompt.text.isNotBlank())
    }
}
```

- [ ] **Step 2: Run, expect fail** → FAIL.

- [ ] **Step 3: Implement, porting the old static list into `seed-data.kt`**

Move the curated phrase/image pairs from the old `RiddleManager` into a private
`val SEED: List<Pair<Prompt, String>>`. `SeedPromptSource.next` picks one;
`SeedMachine.generate` looks up the paired URL and returns `Picture.Ready(url)`,
or `Picture.Failed` if absent.

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Commit**

```bash
git add adapters/
git commit -m "feat(adapter): seed PromptSource and Machine from curated data"
```

### Task 4.2: Persistence adapters (out/persistence)

**Files:**
- Create: `out/persistence/inmemory/InMemoryChallengeRepository.kt`, `out/persistence/sqlite/SqliteChallengeRepository.kt`, `ChallengeRow.kt`, `adapters/src/main/resources/schema.sql`
- Test: `adapters/src/test/kotlin/.../InMemoryChallengeRepositoryTest.kt`, `SqliteChallengeRepositoryIT.kt`

**Interfaces:**
- Consumes: `ChallengeRepository`, `OptimisticLockConflict`, `Challenge.rehydrate`, `Challenge.secretPrompt`.
- Produces: two `ChallengeRepository` implementations. Both enforce optimistic locking: `save` rejects when the stored `version` differs from the in-hand `version`, and persists `version + 1`. Decision: the SQLite adapter uses **Spring Data JDBC** (aggregate-oriented, the most DDD-aligned option) with a hand-written mapping between the domain `Challenge` and a `ChallengeRow`; the domain type is never annotated.

- [ ] **Step 1: Failing test for optimistic locking (in-memory first)**

```kotlin
class InMemoryChallengeRepositoryTest {
    private val repo = InMemoryChallengeRepository()

    @Test fun `saves and finds by id`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        repo.save(c)
        assertEquals(c.id, repo.findById(c.id)?.id)
    }

    @Test fun `rejects a stale version`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        repo.save(c)            // stored version becomes 1
        // a second save carrying the original version 0 must conflict
        assertFailsWith<OptimisticLockConflict> { repo.save(c) }
    }
}
```

- [ ] **Step 2: Run, expect fail** → FAIL.

- [ ] **Step 3: Implement in-memory with version check**

Store a `Map<ChallengeId, Pair<Challenge, Long>>` keyed by id holding the
persisted version; on `save`, compare versions, then store `version + 1` by
re-hydrating a copy with the bumped version. Map guesses, lives, status, picture
to and from storage explicitly.

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Implement SQLite via Spring Data JDBC + `schema.sql`**

Add `org.springframework.boot:spring-boot-starter-data-jdbc` and
`org.xerial:sqlite-jdbc` to `adapters`. Define `schema.sql` with a `challenge`
table (id TEXT PK, prompt TEXT, guesses TEXT, lives INT, status TEXT,
picture_status TEXT, picture_url TEXT, difficulty TEXT, version INT). Implement
`SqliteChallengeRepository` mapping `Challenge` to `ChallengeRow` and back via
`Challenge.rehydrate`, with the version check as a conditional `UPDATE ... WHERE
version = ?` that throws `OptimisticLockConflict` on zero rows affected.

- [ ] **Step 6: Integration test against a temp SQLite file** → green.

Run: `./gradlew :adapters:test --tests "*SqliteChallengeRepositoryIT"` PASS.

- [ ] **Step 7: Commit**

```bash
git add adapters/
git commit -m "feat(adapter): in-memory and SQLite ChallengeRepository with optimistic locking"
```

### Task 4.3: REST web adapter (in/web)

**Files:**
- Create: `in/web/ChallengeController.kt`, `in/web/dto/ChallengeResponse.kt` `GuessRequest.kt`, `in/web/ApiExceptionHandler.kt`, `in/web/SpaForwardingController.kt`
- Test: `adapters/src/test/kotlin/.../ChallengeControllerTest.kt`

**Interfaces:**
- Consumes: `StartChallenge`, `MakeGuess`, `GetChallenge`, `ForfeitChallenge`, and the domain exceptions.
- Produces: REST endpoints `POST /api/challenges`, `GET /api/challenges/{id}`, `POST /api/challenges/{id}/guesses`, `POST /api/challenges/{id}/forfeit`, each returning a `ChallengeResponse` carrying `id`, masked tokens (`[{revealed: bool, word: string?}]`), `livesRemaining`, `status`, and `picture` (`{status, url?}`). The secret prompt is never serialized unless `status != IN_PROGRESS`. An `@RestControllerAdvice` maps `ChallengeNotFound` to 404, `ChallengeAlreadyOver` and `OptimisticLockConflict` to 409, `IllegalArgumentException`/`InvalidGuess` to 422.

- [ ] **Step 1: Failing `@WebMvcTest` slice**

```kotlin
@WebMvcTest(ChallengeController::class)
class ChallengeControllerTest(@Autowired val mvc: MockMvc) {
    @MockkBean lateinit var start: StartChallenge
    @MockkBean lateinit var makeGuess: MakeGuess
    @MockkBean lateinit var getChallenge: GetChallenge
    @MockkBean lateinit var forfeit: ForfeitChallenge

    @Test fun `POST creates a challenge and never leaks the prompt`() {
        every { start.start(any()) } returns Challenge.start(Prompt("hello world"), Lives(6))
        mvc.post("/api/challenges").andExpect {
            status { isOk() }
            jsonPath("$.livesRemaining") { value(6) }
            jsonPath("$.status") { value("IN_PROGRESS") }
            jsonPath("$.picture.status") { value("PENDING") }
            jsonPath("$.maskedPrompt[0].revealed") { value(false) }
        }
    }

    @Test fun `guessing an unknown challenge returns 404`() {
        every { makeGuess.guess(any(), any()) } throws ChallengeNotFound(ChallengeId.new())
        mvc.post("/api/challenges/${'$'}{ChallengeId.new().value}/guesses") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"word":"hello"}"""
        }.andExpect { status { isNotFound() } }
    }
}
```

- [ ] **Step 2: Run, expect fail** → FAIL.

- [ ] **Step 3: Implement the controller, DTOs, mapping, and advice**

Map `Challenge` to `ChallengeResponse`, converting `MaskedToken` to
`{revealed, word}` and `Picture` to `{status, url?}`. The `@RestControllerAdvice`
maps exceptions to the status codes in the Interfaces block. Add
`SpaForwardingController` forwarding non-`/api` unmatched paths to
`/index.html` (no-op until the SPA ships, harmless now).

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Commit**

```bash
git add adapters/
git commit -m "feat(adapter): REST web adapter with error mapping and no prompt leakage"
```

---

## Phase 5: Wiring, async picture pipeline, end-to-end

### Task 5.1: Bean wiring

**Files:**
- Create: `adapters/src/main/kotlin/.../config/BeanConfig.kt`
- Modify: `adapters/src/main/kotlin/.../BeatTheMachineApplication.kt`

**Interfaces:**
- Consumes: every port implementation and use-case service.
- Produces: Spring `@Bean`s wiring `StartChallengeService` (with the picture-enqueue lambda from Task 5.2), `MakeGuessService`, `GetChallengeService`, `ForfeitChallengeService`, choosing the repository implementation by profile/property (`btm.persistence=inmemory|sqlite`).

- [ ] **Step 1: `@SpringBootTest` context-loads test**

```kotlin
@SpringBootTest
class ApplicationContextTest {
    @Test fun `context loads`() {}
}
```

- [ ] **Step 2: Run, expect fail until beans wire** → FAIL/then PASS after Step 3.

- [ ] **Step 3: Implement `BeanConfig`**

Declare `@Bean` factory methods for each service, injecting the chosen
`ChallengeRepository`, `PromptSource`, `Machine`. Select the repository with
`@ConditionalOnProperty(name = ["btm.persistence"], havingValue = ...)`.

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Commit**

```bash
git add adapters/
git commit -m "feat(adapter): bean wiring with profile-selected repository"
```

### Task 5.2: Async picture pre-generation with retry-on-restart

**Files:**
- Create: `adapters/src/main/kotlin/.../config/PicturePregeneration.kt`
- Test: `adapters/src/test/kotlin/.../PicturePregenerationTest.kt`

**Interfaces:**
- Consumes: `Machine`, `ChallengeRepository`, `GetChallenge`.
- Produces: an `@Async` (bounded `ThreadPoolTaskExecutor`) component exposing `enqueue(id: ChallengeId)` that loads the challenge, calls `Machine.generate`, and saves the challenge `withPicture(result)`; on exception saves `Picture.Failed`. An `ApplicationRunner` that on startup finds challenges with `Picture.Pending` and re-enqueues them (retry-on-restart). The enqueue function is the lambda injected into `StartChallengeService`.

- [ ] **Step 1: Failing test (sync executor) that a pending picture becomes ready**

```kotlin
class PicturePregenerationTest {
    @Test fun `generation flips a pending picture to ready and persists it`() {
        val repo = InMemoryChallengeRepository()
        val machine = Machine { Picture.Ready("http://img/1.png") }
        val c = repo.save(Challenge.start(Prompt("hello world"), Lives(6)))
        val pre = PicturePregeneration(machine, repo, directExecutor())
        pre.enqueue(c.id)
        assertEquals(Picture.Ready("http://img/1.png"), repo.findById(c.id)?.picture)
    }

    @Test fun `a failing machine persists a failed picture`() {
        val repo = InMemoryChallengeRepository()
        val machine = Machine { error("boom") }
        val c = repo.save(Challenge.start(Prompt("hello world"), Lives(6)))
        PicturePregeneration(machine, repo, directExecutor()).enqueue(c.id)
        assertEquals(Picture.Failed, repo.findById(c.id)?.picture)
    }
}
```

- [ ] **Step 2: Run, expect fail** → FAIL.

- [ ] **Step 3: Implement the pre-generation component and the startup runner**

Wrap generation in try/catch, saving `Picture.Failed` on error. The component
takes an `Executor` so tests can pass a direct executor. The
`ApplicationRunner` queries pending challenges and re-enqueues.

- [ ] **Step 4: Run to green** → PASS.

- [ ] **Step 5: Commit**

```bash
git add adapters/
git commit -m "feat(adapter): async picture pre-generation with retry-on-restart"
```

### Task 5.3: End-to-end green and cleanup

**Files:**
- Delete: any remaining legacy classes (`RiddleManager`, `RiddleService`, `Riddle`, `GuessResponse`, `StringUtils`, old controllers) and their now-redundant tests once superseded.
- Modify: `README.md` (run instructions, `/health`, persistence property).

**Interfaces:**
- Produces: a clean build with only the new architecture, the legacy code removed.

- [ ] **Step 1: Remove superseded legacy code and tests**

Delete the old `models/`, `services/`, `utils/`, and `controllers/` packages and
the characterization tests that referenced them (their intent now lives in
domain tests). Keep `FavIconController` only if still needed.

- [ ] **Step 2: Full suite, not incremental**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL, all modules green, coverage not regressed.

- [ ] **Step 3: Boot and exercise the API manually**

Run: `./gradlew :adapters:bootRun` then:
```bash
curl -fsS -XPOST localhost:80/api/challenges
curl -fsS localhost:80/api/challenges/<id>
curl -fsS -XPOST localhost:80/api/challenges/<id>/guesses -H 'content-type: application/json' -d '{"word":"hello"}'
```
Expected: a challenge with `picture.status` moving `PENDING` to `READY`, guesses updating the masked prompt and lives.

- [ ] **Step 4: Update README**

Document the new run command, `/health`, and `btm.persistence` property.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: remove legacy static-store code; backend refactor complete"
```

---

## Out of scope (separate plans)

- The React + TypeScript SPA (spec phase 6). Write its plan after this backend
  is merged so the endpoints are concrete.
- The Spring AI `LlmPromptSource` and image `Machine`. The ports and the
  `Pending/Ready/Failed` lifecycle are built here; the implementations are a
  later plan.

## Self-Review

Spec coverage: ubiquitous language (Phase 2 types), three-module hexagonal
layout (Phase 1), stateful aggregate with invariants including corrected
forfeit (Task 2.3), value objects killing primitive obsession (2.1, 2.2),
single masking rule (2.1), use cases and outbound ports (Phase 3), sync state
persistence + async picture with retry (5.2), optimistic locking (4.2), JSON
REST with error mapping and no prompt leakage (4.3), seed adapters with ports
ready for Spring AI (4.1), deploy fixes (1.2). The SPA and the AI
implementations are explicitly deferred to follow-up plans, matching the spec's
Out of Scope. No placeholders remain. Type names (`Challenge`, `Guess`,
`Prompt`, `MaskedPrompt`/`MaskedToken`, `Lives`, `Picture`, `ChallengeStatus`,
`GuessOutcome`, the four use-case interfaces, the three ports) are consistent
across tasks.
