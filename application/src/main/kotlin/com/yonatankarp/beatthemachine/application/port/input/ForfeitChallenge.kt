package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId

interface ForfeitChallenge {
    fun forfeit(id: ChallengeId): Challenge
}
