package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.slf4j.LoggerFactory

/**
 * Drives the asynchronous picture pipeline. A freshly started challenge is
 * persisted with [Picture.Pending]; [enqueue] launches the work into [scope], which
 * loads the challenge, asks the [Machine] to generate the picture, and persists
 * the result onto the latest version of the aggregate. A generation failure is
 * recorded as [Picture.Failed] rather than left pending forever.
 *
 * [scope] is the application-wide picture scope: a [kotlinx.coroutines.SupervisorJob]
 * over a parallelism-bounded dispatcher, so one failed task never cancels its
 * siblings and the work is throttled to a fixed number of concurrent generations.
 *
 * [maxQueued] bounds admission: [enqueue] takes a permit non-blockingly and sheds the
 * request if none is free, so a burst of starts (against a slow, network-bound machine)
 * cannot accumulate unbounded coroutines. A shed picture stays [Picture.Pending] and is
 * recovered by [retryPending] on the next restart.
 */
class PicturePregeneration(
    private val machine: Machine,
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
    private val findPendingChallenges: FindPendingChallenges,
    private val scope: CoroutineScope,
    maxQueued: Int = DEFAULT_MAX_QUEUED,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val admission = Semaphore(maxQueued)

    fun enqueue(id: ChallengeId) {
        // Non-suspending admission: never block the calling (Netty event-loop) thread.
        if (!admission.tryAcquire()) {
            logger.warn("picture queue full; shedding {} — retryPending recovers it on restart", id)
            return
        }
        scope.launch {
            try {
                runCatching { generate(id) }
                    .onFailure { logger.error("Unexpected failure generating picture for challenge {}", id, it) }
            } finally {
                admission.release() // also runs on cancellation, so permits are never leaked
            }
        }
    }

    /** Re-enqueues every challenge whose picture is still pending (retry-on-restart). */
    suspend fun retryPending() {
        findPendingChallenges().forEach { enqueue(it.id) }
    }

    private suspend fun generate(id: ChallengeId) {
        val picture =
            try {
                val current = findChallengeById(id) ?: return
                machine generate current.secretPrompt()
            } catch (e: CancellationException) {
                throw e // never swallow cancellation: it must propagate to keep structured concurrency intact
            } catch (e: Exception) {
                logger.warn("Picture generation failed for challenge {}", id, e)
                Picture.Failed
            }
        persistPicture(id, picture)
    }

    /**
     * Writes only the picture, onto the latest persisted version. A concurrent
     * guess can bump the version between generation and this write, so we reload
     * and retry on conflict rather than clobbering newer game state or losing the
     * picture to a swallowed [OptimisticLockConflict].
     */
    private suspend fun persistPicture(
        id: ChallengeId,
        picture: Picture,
    ) {
        repeat(MAX_SAVE_ATTEMPTS) {
            val latest = findChallengeById(id) ?: return
            if (latest.picture !is Picture.Pending) return // already resolved by another writer
            try {
                storeChallenge(latest.withPicture(picture))
                return
            } catch (_: OptimisticLockConflict) {
                // concurrent write bumped the version; reload and retry
            }
        }
        logger.warn("Gave up persisting picture for challenge {} after {} attempts", id, MAX_SAVE_ATTEMPTS)
    }

    private companion object {
        const val MAX_SAVE_ATTEMPTS = 3

        // Matches the former ThreadPoolTaskExecutor queueCapacity (the bounded backlog
        // that fed core 2 / max 4 worker threads). Beyond this, starts are shed rather
        // than queued, bounding memory under a burst against a slow machine.
        const val DEFAULT_MAX_QUEUED = 50
    }
}
