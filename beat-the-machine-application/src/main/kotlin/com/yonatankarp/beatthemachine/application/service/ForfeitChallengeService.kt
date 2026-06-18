package com.yonatankarp.beatthemachine.application.service

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId

class ForfeitChallengeService(
    private val repository: ChallengeRepository,
) : ForfeitChallenge {
    override fun forfeit(id: ChallengeId): Challenge {
        val challenge = repository.findById(id) ?: throw ChallengeNotFound(id)
        challenge.forfeit()
        return repository.save(challenge)
    }
}
