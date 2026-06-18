package com.yonatankarp.beatthemachine.application.service

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Guess
import com.yonatankarp.beatthemachine.domain.GuessOutcome

class MakeGuessService(
    private val repository: ChallengeRepository,
) : MakeGuess {
    override fun guess(
        id: ChallengeId,
        guess: Guess,
    ): Pair<Challenge, GuessOutcome> {
        val challenge = repository.findById(id) ?: throw ChallengeNotFound(id)
        val outcome = challenge.makeGuess(guess)
        return repository.save(challenge) to outcome
    }
}
