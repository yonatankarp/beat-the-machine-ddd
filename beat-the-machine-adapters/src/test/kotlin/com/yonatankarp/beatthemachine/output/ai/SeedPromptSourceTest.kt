package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SeedPromptSourceTest {
    @Test
    fun `returns a prompt from the curated seed set`() =
        runTest {
            // Given
            val seedPrompts = SEED.map { it.first }.toSet()

            // When / Then
            repeat(20) {
                val prompt = SeedPromptSource().next(Difficulty.MEDIUM)
                assertTrue(prompt in seedPrompts, "returned prompt must come from the curated SEED set")
            }
        }
}
