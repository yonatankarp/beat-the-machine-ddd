package com.yonatankarp.beatthemachine.domain

import java.util.UUID

@JvmInline
value class ChallengeId(
    val value: UUID,
) {
    companion object {
        fun new(): ChallengeId = ChallengeId(UUID.randomUUID())
    }
}
