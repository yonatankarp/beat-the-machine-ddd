package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue

val SeedPromptSourceSuite by testSuite {
    given("a seed prompt source") {
        whenever("answering a prompt query") {
            then("it returns a prompt from the curated seed set") {
                val seedPrompts = SEED.map { it.first }.toSet()
                repeat(20) {
                    val prompt = SeedPromptSource() answer PromptSource.Query(Difficulty.MEDIUM)
                    (prompt in seedPrompts).shouldBeTrue()
                }
            }
        }
    }
}
