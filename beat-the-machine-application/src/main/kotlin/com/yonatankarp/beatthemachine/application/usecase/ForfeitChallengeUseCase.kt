package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class ForfeitChallengeUseCase(
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
) : ForfeitChallenge {
    override suspend fun invoke(id: ChallengeId): Challenge {
        val challenge = findChallengeById(id) ?: throw ChallengeNotFound(id)
        return storeChallenge(challenge.forfeit())
    }
}
