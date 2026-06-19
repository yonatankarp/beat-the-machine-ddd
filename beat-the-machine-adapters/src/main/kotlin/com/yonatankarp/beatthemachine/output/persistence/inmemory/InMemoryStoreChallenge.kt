package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class InMemoryStoreChallenge(
    private val store: InMemoryChallengeStore,
) : StoreChallenge {
    override suspend fun invoke(challenge: Challenge): Challenge =
        // Atomic check-then-act: compute runs the version check and the write under the
        // map's per-key lock, so two concurrent coroutines on the same id cannot both pass
        // the check and defeat optimistic locking (the picture pipeline races MakeGuess).
        store.byId.compute(challenge.id) { _, existing ->
            if (existing != null && existing.version != challenge.version) {
                throw OptimisticLockConflict(challenge.id)
            }
            Challenge.rehydrate(
                id = challenge.id,
                prompt = challenge.secretPrompt(),
                guesses = challenge.guesses,
                lives = challenge.lives,
                status = challenge.status,
                picture = challenge.picture,
                difficulty = challenge.difficulty,
                version = challenge.version + 1,
            )
        }!!
}
