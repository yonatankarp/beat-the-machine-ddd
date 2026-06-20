package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.CommandHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge

interface StoreChallenge : CommandHandler<StoreChallenge.Command, Challenge> {
    data class Command(
        val challenge: Challenge,
    )
}
