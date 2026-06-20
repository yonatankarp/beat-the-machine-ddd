package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

fun interface FindChallengeById {
    suspend operator fun invoke(id: ChallengeId): Challenge?
}
