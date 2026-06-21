package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeStore
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindChallengeById
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindPendingChallenges
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryStoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
val PicturePregenerationSuite by testSuite {
    test("generation flips a pending picture to ready and persists it") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val machine = Machine { Picture.Ready("http://img/1.png") }
            val c = storeChallenge(mediumChallenge())

            // When
            PicturePregeneration(machine, findById, storeChallenge, findPending, this, 50).enqueue(c.id)
            advanceUntilIdle()

            // Then
            findById(c.id)?.picture shouldBe Picture.Ready("http://img/1.png")
        }
    }

    test("a failing machine persists a failed picture") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val machine = Machine { error("boom") }
            val c = storeChallenge(mediumChallenge())

            // When
            PicturePregeneration(machine, findById, storeChallenge, findPending, this, 50).enqueue(c.id)
            advanceUntilIdle()

            // Then
            findById(c.id)?.picture shouldBe Picture.Failed
        }
    }

    test("enqueue for an unknown challenge is a no-op") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val machine = Machine { Picture.Ready("http://img/1.png") }

            // When
            PicturePregeneration(machine, findById, storeChallenge, findPending, this, 50).enqueue(aChallengeId())
            advanceUntilIdle()
        }
    }

    test("retryPending re-enqueues every challenge still awaiting a picture") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val machine = Machine { Picture.Ready("http://img/retry.png") }
            val pending = storeChallenge(mediumChallenge())
            val done = storeChallenge(mediumChallenge(prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/done.png")))

            // When
            PicturePregeneration(machine, findById, storeChallenge, findPending, this, 50).retryPending()
            advanceUntilIdle()

            // Then
            findById(pending.id)?.picture shouldBe Picture.Ready("http://img/retry.png")
            findById(done.id)?.picture shouldBe Picture.Ready("http://img/done.png")
        }
    }

    test("a concurrent version bump is retried so the picture still persists") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val seeded = storeChallenge(mediumChallenge())

            var firstStore = true
            val racingStore =
                StoreChallenge { challenge ->
                    if (firstStore && challenge.picture is Picture.Ready) {
                        firstStore = false
                        storeChallenge(findById(challenge.id) ?: challenge)
                        throw OptimisticLockConflict(challenge.id)
                    }
                    storeChallenge(challenge)
                }
            val machine = Machine { Picture.Ready("http://img/raced.png") }

            // When
            PicturePregeneration(machine, findById, racingStore, findPending, this, 50).enqueue(seeded.id)
            advanceUntilIdle()

            // Then
            findById(seeded.id)?.picture shouldBe Picture.Ready("http://img/raced.png")
        }
    }

    test("enqueue sheds work once the admission bound is full") {
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findById = InMemoryFindChallengeById(store)
            val findPending = InMemoryFindPendingChallenges(store)
            val machine = Machine { Picture.Ready("http://img/admitted.png") }
            val admitted = storeChallenge(mediumChallenge())
            val shed = storeChallenge(mediumChallenge(prompt = "foo bar".asPrompt()))
            val pregeneration = PicturePregeneration(machine, findById, storeChallenge, findPending, this, 1)

            // When
            pregeneration.enqueue(admitted.id)
            pregeneration.enqueue(shed.id)
            advanceUntilIdle()

            // Then
            findById(admitted.id)?.picture shouldBe Picture.Ready("http://img/admitted.png")
            findById(shed.id)?.picture shouldBe Picture.Pending
        }
    }
}
