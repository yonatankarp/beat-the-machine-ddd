package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeStore
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindChallengeById
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindPendingChallenges
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryStoreChallenge
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
            val c = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

            // When
            pregeneration(machine, this).enqueue(c.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/1.png"), findById(c.id)?.picture)
        }

    @Test
    fun `a failing machine persists a failed picture`() =
        runTest {
            // Given
            val machine = Machine { error("boom") }
            val c = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

            // When
            pregeneration(machine, this).enqueue(c.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Failed, findById(c.id)?.picture)
        }

    @Test
    fun `enqueue for an unknown challenge is a no-op`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/1.png") }

            // When
            pregeneration(machine, this).enqueue(ChallengeId.new())
            advanceUntilIdle()
        }

    @Test
    fun `retryPending re-enqueues every challenge still awaiting a picture`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/retry.png") }
            val pending = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))
            val done = storeChallenge(Challenge.start(Prompt("foo bar"), Lives(6)).withPicture(Picture.Ready("http://img/done.png")))

            // When
            pregeneration(machine, this).retryPending()
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/retry.png"), findById(pending.id)?.picture)
            assertEquals(Picture.Ready("http://img/done.png"), findById(done.id)?.picture)
        }

    @Test
    fun `a concurrent version bump is retried so the picture still persists`() =
        runTest {
            // Given
            val seeded = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

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
            pregeneration(machine, this, racingStore).enqueue(seeded.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/raced.png"), findById(seeded.id)?.picture)
        }

    @Test
    fun `enqueue sheds work once the admission bound is full`() =
        runTest {
            // Given
            val machine = Machine { Picture.Ready("http://img/admitted.png") }
            val admitted = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))
            val shed = storeChallenge(Challenge.start(Prompt("foo bar"), Lives(6)))
            val pregeneration = pregeneration(machine, this, maxQueued = 1)

            // When
            pregeneration.enqueue(admitted.id)
            pregeneration.enqueue(shed.id)
            advanceUntilIdle()

            // Then
            assertEquals(Picture.Ready("http://img/admitted.png"), findById(admitted.id)?.picture)
            assertEquals(Picture.Pending, findById(shed.id)?.picture)
        }

    private fun pregeneration(
        machine: Machine,
        scope: CoroutineScope,
        store: StoreChallenge = storeChallenge,
        maxQueued: Int = 50,
    ) = PicturePregeneration(machine, findById, store, findPending, scope, maxQueued)
}
