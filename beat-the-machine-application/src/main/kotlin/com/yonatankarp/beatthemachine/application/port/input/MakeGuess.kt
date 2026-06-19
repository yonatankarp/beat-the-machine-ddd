package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome

fun interface MakeGuess {
    suspend operator fun invoke(
        id: ChallengeId,
        guess: Guess,
    ): Pair<Challenge, GuessOutcome>
}
