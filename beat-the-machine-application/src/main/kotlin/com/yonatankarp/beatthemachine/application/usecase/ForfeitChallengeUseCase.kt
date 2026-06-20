package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class ForfeitChallengeUseCase(
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
) : ForfeitChallenge {
    override suspend fun handle(command: ForfeitChallenge.Command): Challenge {
        val challenge =
            (findChallengeById answer FindChallengeById.Query(command.id)) ?: throw ChallengeNotFound(command.id)
        return storeChallenge handle StoreChallenge.Command(challenge.forfeit())
    }
}
