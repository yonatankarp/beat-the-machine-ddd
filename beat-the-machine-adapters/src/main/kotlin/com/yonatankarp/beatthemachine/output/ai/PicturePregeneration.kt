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
        if (!admission.tryAcquire()) {
            logger.warn("picture queue full; shedding {} — retryPending recovers it on restart", id)
            return
        }
        scope.launch {
            try {
                runCatching { generate(id) }
                    .onFailure { logger.error("Unexpected failure generating picture for challenge {}", id, it) }
            } finally {
                admission.release()
            }
        }
    }

    suspend fun retryPending() {
        findPendingChallenges().forEach { enqueue(it.id) }
    }

    private suspend fun generate(id: ChallengeId) {
        val picture =
            try {
                val current = findChallengeById(id) ?: return
                machine generate current.secretPrompt()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Picture generation failed for challenge {}", id, e)
                Picture.Failed
            }
        persistPicture(id, picture)
    }

    private suspend fun persistPicture(
        id: ChallengeId,
        picture: Picture,
    ) {
        repeat(MAX_SAVE_ATTEMPTS) {
            val latest = findChallengeById(id) ?: return
            if (latest.picture !is Picture.Pending) return
            try {
                storeChallenge(latest.withPicture(picture))
                return
            } catch (_: OptimisticLockConflict) {
                logger.debug("Version conflict persisting picture for challenge {}; reloading and retrying", id)
            }
        }
        logger.warn("Gave up persisting picture for challenge {} after {} attempts", id, MAX_SAVE_ATTEMPTS)
    }

    private companion object {
        const val MAX_SAVE_ATTEMPTS = 3
        const val DEFAULT_MAX_QUEUED = 50
    }
}
