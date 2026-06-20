package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.application.port.CommandHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome

interface MakeGuess : CommandHandler<MakeGuess.Command, Pair<Challenge, GuessOutcome>> {
    data class Command(
        val id: ChallengeId,
        val word: Guess,
    )
}
