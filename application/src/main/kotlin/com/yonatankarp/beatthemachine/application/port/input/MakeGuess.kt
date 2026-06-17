package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Guess
import com.yonatankarp.beatthemachine.domain.GuessOutcome

interface MakeGuess {
    fun guess(
        id: ChallengeId,
        guess: Guess,
    ): Pair<Challenge, GuessOutcome>
}
