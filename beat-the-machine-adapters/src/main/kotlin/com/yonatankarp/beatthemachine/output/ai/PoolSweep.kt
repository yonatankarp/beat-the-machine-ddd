package com.yonatankarp.beatthemachine.output.ai

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PoolSweep(
    private val replenisher: ChallengePoolReplenisher,
) {
    @Scheduled(fixedDelayString = "\${btm.pool.sweep-ms:60000}")
    fun sweep() {
        replenisher.warmUp()
    }
}
