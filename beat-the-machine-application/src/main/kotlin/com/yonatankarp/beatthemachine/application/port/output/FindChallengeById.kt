package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

/** Looks up a single challenge by its id, or `null` when none exists. */
fun interface FindChallengeById {
    operator fun invoke(id: ChallengeId): Challenge?
}
