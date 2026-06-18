package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId

interface GetChallenge {
    fun get(id: ChallengeId): Challenge
}
