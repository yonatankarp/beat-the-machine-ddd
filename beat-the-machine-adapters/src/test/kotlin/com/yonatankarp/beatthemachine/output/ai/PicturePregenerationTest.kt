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
import org.junit.jupiter.api.Test
import java.util.concurrent.Executor
import kotlin.test.assertEquals

class PicturePregenerationTest {
    private fun directExecutor() = Executor { it.run() }

    private val store = InMemoryChallengeStore()
    private val storeChallenge = InMemoryStoreChallenge(store)
    private val findById = InMemoryFindChallengeById(store)
    private val findPending = InMemoryFindPendingChallenges(store)

    @Test
    fun `generation flips a pending picture to ready and persists it`() {
        val machine = Machine { Picture.Ready("http://img/1.png") }
        val c = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

        PicturePregeneration(machine, findById, storeChallenge, findPending, directExecutor()).enqueue(c.id)

        assertEquals(Picture.Ready("http://img/1.png"), findById(c.id)?.picture)
    }

    @Test
    fun `a failing machine persists a failed picture`() {
        val machine = Machine { error("boom") }
        val c = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

        PicturePregeneration(machine, findById, storeChallenge, findPending, directExecutor()).enqueue(c.id)

        assertEquals(Picture.Failed, findById(c.id)?.picture)
    }

    @Test
    fun `enqueue for an unknown challenge is a no-op`() {
        val machine = Machine { Picture.Ready("http://img/1.png") }

        // Must not throw; nothing to persist.
        PicturePregeneration(machine, findById, storeChallenge, findPending, directExecutor()).enqueue(ChallengeId.new())
    }

    @Test
    fun `retryPending re-enqueues every challenge still awaiting a picture`() {
        val machine = Machine { Picture.Ready("http://img/retry.png") }
        val pending = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))
        val done = storeChallenge(Challenge.start(Prompt("foo bar"), Lives(6)).withPicture(Picture.Ready("http://img/done.png")))

        PicturePregeneration(machine, findById, storeChallenge, findPending, directExecutor()).retryPending()

        assertEquals(Picture.Ready("http://img/retry.png"), findById(pending.id)?.picture)
        // an already-ready picture is left untouched
        assertEquals(Picture.Ready("http://img/done.png"), findById(done.id)?.picture)
    }

    @Test
    fun `a concurrent version bump is retried so the picture still persists`() {
        val seeded = storeChallenge(Challenge.start(Prompt("hello world"), Lives(6)))

        // Simulate a guess landing exactly once between generation and the picture
        // save: the first store sees a stale version and conflicts, the reload wins.
        var firstStore = true
        val racingStore =
            StoreChallenge { challenge ->
                if (firstStore && challenge.picture is Picture.Ready) {
                    firstStore = false
                    storeChallenge(findById(challenge.id) ?: challenge) // competing write bumps the version
                    throw OptimisticLockConflict(challenge.id)
                }
                storeChallenge(challenge)
            }
        val machine = Machine { Picture.Ready("http://img/raced.png") }

        PicturePregeneration(machine, findById, racingStore, findPending, directExecutor()).enqueue(seeded.id)

        assertEquals(Picture.Ready("http://img/raced.png"), findById(seeded.id)?.picture)
    }
}
