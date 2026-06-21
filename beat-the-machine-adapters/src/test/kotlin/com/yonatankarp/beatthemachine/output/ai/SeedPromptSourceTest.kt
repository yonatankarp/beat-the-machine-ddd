package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue

val SeedPromptSourceSuite by testSuite {
    test("returns a prompt from the curated seed set") {
        // Given
        val seedPrompts = SEED.map { it.first }.toSet()

        // When / Then
        repeat(20) {
            val prompt = SeedPromptSource().next(Difficulty.MEDIUM)
            (prompt in seedPrompts).shouldBeTrue()
        }
    }
}
