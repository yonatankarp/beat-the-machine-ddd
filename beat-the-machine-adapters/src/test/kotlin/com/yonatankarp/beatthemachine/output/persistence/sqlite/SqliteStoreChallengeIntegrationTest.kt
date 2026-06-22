package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallengeWithGuesses
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val SqliteStoreChallengeITSuite by testSuite {
    given("a fresh challenge") {
        val adapters by setup { newSqliteAdapters() }
        val challenge by setup { mediumChallenge(prompt = "pixel art cat".asPrompt()) }

        whenever("storing it") {
            val saved by action {
                val (storeChallenge) = adapters
                storeChallenge handle StoreChallenge.Command(challenge)
            }

            then("it stores the challenge and bumps the version") {
                saved.version shouldBe 1L
            }
        }
    }

    given("a stored challenge") {
        val adapters by setup { newSqliteAdapters() }
        val challenge by setup { mediumChallenge(prompt = "sequential".asPrompt()) }
        val stored by setup {
            val (storeChallenge) = adapters
            storeChallenge handle StoreChallenge.Command(challenge)
        }

        whenever("storing it again sequentially") {
            val restored by action {
                val (storeChallenge) = adapters
                storeChallenge handle StoreChallenge.Command(stored)
            }

            then("it allows sequential stores with updated versions") {
                stored.version shouldBe 1L
                restored.version shouldBe 2L
            }
        }

        whenever("storing a stale version on second store") {
            val storedVersion by action { stored.version }
            val result by action {
                val (storeChallenge) = adapters
                shouldThrow<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(challenge) }
            }

            then("it rejects the stale version") {
                storedVersion shouldBe 1L
                result.id shouldBe challenge.id
            }
        }
    }

    given("challenges of every difficulty level") {
        val adapters by setup { newSqliteAdapters() }

        whenever("storing and reloading them") {
            val foundDifficulties by action {
                val (storeChallenge, findChallengeById) = adapters
                Difficulty.entries.map { diff ->
                    val c = Challenge.start("test prompt".asPrompt(), 2.lives(), difficulty = diff)
                    storeChallenge handle StoreChallenge.Command(c)
                    val found = findChallengeById answer FindChallengeById.Query(c.id)
                    found.shouldNotBeNull()
                    found.difficulty
                }
            }

            then("it persists all difficulty levels") {
                foundDifficulties shouldBe Difficulty.entries
            }
        }
    }

    given("challenges with pending, ready and failed pictures") {
        val adapters by setup { newSqliteAdapters() }
        val pending by setup { mediumChallenge(prompt = "pending pic".asPrompt()) }
        val ready by setup { mediumChallenge(prompt = "ready pic".asPrompt(), picture = readyPicture()) }
        val failed by setup { mediumChallenge(prompt = "failed pic".asPrompt(), picture = failedPicture()) }

        whenever("storing and reloading them") {
            val pictures by action {
                val (storeChallenge, findChallengeById) = adapters
                storeChallenge handle StoreChallenge.Command(pending)
                storeChallenge handle StoreChallenge.Command(ready)
                storeChallenge handle StoreChallenge.Command(failed)
                listOf(
                    (findChallengeById answer FindChallengeById.Query(pending.id))?.picture,
                    (findChallengeById answer FindChallengeById.Query(ready.id))?.picture,
                    (findChallengeById answer FindChallengeById.Query(failed.id))?.picture,
                )
            }

            then("it persists picture states correctly") {
                pictures shouldBe
                    listOf(
                        Picture.Pending,
                        Picture.Ready("https://example.com/img.png"),
                        Picture.Failed,
                    )
            }
        }
    }

    given("a challenge with a guess containing the old delimiter") {
        val adapters by setup { newSqliteAdapters() }
        val challenge by setup {
            mediumChallengeWithGuesses(
                prompt = "pipe guess".asPrompt(),
                guesses = setOf(Guess("a|b")),
                lives = 2.lives(),
            )
        }

        whenever("storing and reloading it") {
            val found by action {
                val (storeChallenge, findChallengeById) = adapters
                storeChallenge handle StoreChallenge.Command(challenge)
                findChallengeById answer FindChallengeById.Query(challenge.id)
            }

            then("it preserves the guess as one value") {
                val reloaded = found.shouldNotBeNull()
                reloaded.guesses shouldBe setOf(Guess("a|b"))
            }
        }
    }

    given("a row with an unknown picture status") {
        val adapters by setup { newSqliteAdapters() }
        val challenge by setup { mediumChallenge(prompt = "bad status".asPrompt()) }

        whenever("loading it") {
            val result by action {
                val (storeChallenge, findChallengeById, _, _, _, jdbc) = adapters
                storeChallenge handle StoreChallenge.Command(challenge)
                jdbc.update("UPDATE challenge SET picture_status = ? WHERE id = ?", "BROKEN", challenge.id.value.toString())
                shouldThrow<IllegalStateException> {
                    findChallengeById answer FindChallengeById.Query(challenge.id)
                }
            }

            then("it fails instead of treating the row as pending") {
                result.message shouldBe "Unknown picture_status BROKEN for challenge ${challenge.id.value}"
            }
        }
    }
}
