package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.entity.Challenge

fun interface StoreChallenge {
    suspend operator fun invoke(challenge: Challenge): Challenge
}
