package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class InMemoryStoreChallenge(
    private val store: InMemoryChallengeStore,
) : StoreChallenge {
    override suspend fun invoke(challenge: Challenge): Challenge =
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
