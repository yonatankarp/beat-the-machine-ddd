package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import org.springframework.scheduling.annotation.Scheduled

val PoolSweepSuite by testSuite {
    given("the pool sweep schedule") {
        whenever("inspecting its scheduled trigger") {
            then("it waits for the sweep interval before the first sweep") {
                val scheduled = PoolSweep::class.java.getDeclaredMethod("sweep").getAnnotation(Scheduled::class.java)

                scheduled.fixedDelayString shouldBe "\${btm.pool.sweep-ms:60000}"
                scheduled.initialDelayString shouldBe "\${btm.pool.sweep-ms:60000}"
            }
        }
    }
}
