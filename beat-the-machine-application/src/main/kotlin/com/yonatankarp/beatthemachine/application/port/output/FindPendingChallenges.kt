package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.entity.Challenge

fun interface FindPendingChallenges {
    suspend operator fun invoke(): List<Challenge>
}
