package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.application.port.CommandHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty

interface StartChallenge : CommandHandler<StartChallenge.Command, Challenge> {
    data class Command(
        val difficulty: Difficulty,
    )
}
