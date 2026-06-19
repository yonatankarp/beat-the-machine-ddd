package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome

class MakeGuessUseCase(
    private val findChallengeById: FindChallengeById,
    private val storeChallenge: StoreChallenge,
) : MakeGuess {
    override suspend fun invoke(
        id: ChallengeId,
        guess: Guess,
    ): Pair<Challenge, GuessOutcome> {
        val challenge = findChallengeById(id) ?: throw ChallengeNotFound(id)
        val (updated, outcome) = challenge.makeGuess(guess)
        return storeChallenge(updated) to outcome
    }
}
