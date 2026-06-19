package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

/**
 * Returns every challenge whose picture is still [Picture.Pending], used by the
 * startup retry runner to re-enqueue picture generation for challenges that were
 * created but never had their picture produced.
 */
fun interface FindPendingChallenges {
    suspend operator fun invoke(): List<Challenge>
}
