package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import org.slf4j.LoggerFactory
import java.util.concurrent.Executor

/**
 * Drives the asynchronous picture pipeline. A freshly started challenge is
 * persisted with [Picture.Pending]; [enqueue] hands the work to [executor], which
 * loads the challenge, asks the [Machine] to generate the picture, and persists
 * the result onto the latest version of the aggregate. A generation failure is
 * recorded as [Picture.Failed] rather than left pending forever.
 */
class PicturePregeneration(
    private val machine: Machine,
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
    private val findPendingChallenges: FindPendingChallenges,
    private val executor: Executor,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun enqueue(id: ChallengeId) {
        executor.execute {
            runCatching { generate(id) }
                .onFailure { logger.error("Unexpected failure generating picture for challenge {}", id, it) }
        }
    }

    /** Re-enqueues every challenge whose picture is still pending (retry-on-restart). */
    fun retryPending() {
        findPendingChallenges().forEach { enqueue(it.id) }
    }

    private fun generate(id: ChallengeId) {
        val picture =
            try {
                val current = findChallengeById(id) ?: return
                machine generate current.secretPrompt()
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
    private fun persistPicture(
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
    }
}
