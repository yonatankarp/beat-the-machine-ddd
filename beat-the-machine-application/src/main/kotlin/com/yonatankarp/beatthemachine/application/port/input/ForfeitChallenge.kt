package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.application.port.CommandHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

interface ForfeitChallenge : CommandHandler<ForfeitChallenge.Command, Challenge> {
    data class Command(
        val id: ChallengeId,
    )
}
