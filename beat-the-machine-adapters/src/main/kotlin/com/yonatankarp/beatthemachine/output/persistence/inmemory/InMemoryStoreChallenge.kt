package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class InMemoryStoreChallenge(
    private val store: InMemoryChallengeStore,
) : StoreChallenge {
    override fun invoke(challenge: Challenge): Challenge {
        val existing = store.byId[challenge.id]
        if (existing != null && existing.version != challenge.version) {
            throw OptimisticLockConflict(challenge.id)
        }
        val persisted =
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
        store.byId[challenge.id] = persisted
        return persisted
    }
}
