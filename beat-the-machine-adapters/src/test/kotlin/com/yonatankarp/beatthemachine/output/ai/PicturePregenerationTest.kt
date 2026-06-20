package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PicturePregenerationTest {
    private val store = InMemoryChallengeStore()
    private val storeChallenge = InMemoryStoreChallenge(store)
    private val findById = InMemoryFindChallengeById(store)
    private val findPending = InMemoryFindPendingChallenges(store)

    @Test
    fun `generation flips a pending picture to ready and persists it`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/1.png") }
            val c = storeChallenge(mediumChallenge())

            // When
            pregeneration(machine, this).enqueue(c.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/1.png"), (findById answer FindChallengeById.Query(c.id))?.picture)
        }

    @Test
    fun `a failing machine persists a failed picture`() =
        runTest {
            // Given
            val machine = Machine { error("boom") }
            val c = storeChallenge(mediumChallenge())

            // When
            pregeneration(machine, this).enqueue(c.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Failed, (findById answer FindChallengeById.Query(c.id))?.picture)
        }

    @Test
    fun `enqueue for an unknown challenge is a no-op`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/1.png") }

            // When
            pregeneration(machine, this).enqueue(aChallengeId())
            advanceUntilIdle()
        }

    @Test
    fun `retryPending re-enqueues every challenge still awaiting a picture`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/retry.png") }
            val pending = storeChallenge(mediumChallenge())
            val done = storeChallenge(mediumChallenge(prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/done.png")))

            // When
            pregeneration(machine, this).retryPending()
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/retry.png"), (findById answer FindChallengeById.Query(pending.id))?.picture)
            assertEquals(Picture.Ready("http://img/done.png"), (findById answer FindChallengeById.Query(done.id))?.picture)
        }

    @Test
    fun `a concurrent version bump is retried so the picture still persists`() =
        runTest {
            // Given
            val seeded = storeChallenge(mediumChallenge())

            var firstStore = true
            val racingStore =
                StoreChallenge { challenge ->
                    if (firstStore && challenge.picture is Picture.Ready) {
                        firstStore = false
                        storeChallenge(findById answer FindChallengeById.Query(challenge.id) ?: challenge)
                        throw OptimisticLockConflict(challenge.id)
                    }
                    storeChallenge(challenge)
                }
            val machine = Machine { Picture.Ready("http://img/raced.png") }

            // When
            pregeneration(machine, this, racingStore).enqueue(seeded.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/raced.png"), (findById answer FindChallengeById.Query(seeded.id))?.picture)
        }

    @Test
    fun `enqueue sheds work once the admission bound is full`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/admitted.png") }
            val admitted = storeChallenge(mediumChallenge())
            val shed = storeChallenge(mediumChallenge(prompt = "foo bar".asPrompt()))
            val pregeneration = pregeneration(machine, this, maxQueued = 1)

            // When
            pregeneration.enqueue(admitted.id)
            pregeneration.enqueue(shed.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/admitted.png"), (findById answer FindChallengeById.Query(admitted.id))?.picture)
            assertEquals(Picture.Pending, (findById answer FindChallengeById.Query(shed.id))?.picture)
        }

    private fun pregeneration(
        machine: Machine,
        scope: CoroutineScope,
        store: StoreChallenge = storeChallenge,
        maxQueued: Int = 50,
    ) = PicturePregeneration(machine, findById, store, findPending, scope, maxQueued)
}
