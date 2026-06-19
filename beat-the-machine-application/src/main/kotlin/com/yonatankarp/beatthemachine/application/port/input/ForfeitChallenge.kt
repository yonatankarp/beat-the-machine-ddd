package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

fun interface ForfeitChallenge {
    suspend operator fun invoke(id: ChallengeId): Challenge
}
