package com.yonatankarp.beatthemachine.out.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.port.out.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import java.util.concurrent.ConcurrentHashMap

class InMemoryChallengeRepository : ChallengeRepository {
    // Keyed by ChallengeId; the stored challenge already carries the persisted version.
    private val store = ConcurrentHashMap<ChallengeId, Challenge>()

    override fun save(challenge: Challenge): Challenge {
        val existing = store[challenge.id]
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
        store[challenge.id] = persisted
        return persisted
    }

    override fun findById(id: ChallengeId): Challenge? = store[id]
}
