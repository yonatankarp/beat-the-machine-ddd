package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome

class MakeGuessUseCase(
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
) : MakeGuess {
    override suspend fun handle(command: MakeGuess.Command): Pair<Challenge, GuessOutcome> {
        val challenge =
            (findChallengeById answer FindChallengeById.Query(command.id)) ?: throw ChallengeNotFound(command.id)
        val (updated, outcome) = challenge.makeGuess(command.word)
        return storeChallenge(updated) to outcome
    }
}
