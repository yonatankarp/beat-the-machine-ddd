package com.yonatankarp.beatthemachine.out.ai

import com.yonatankarp.beatthemachine.domain.Difficulty
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SeedPromptSourceTest {
    @Test
    fun `returns a non-blank curated prompt`() {
        val prompt = SeedPromptSource().next(Difficulty.MEDIUM)
        assertTrue(prompt.text.isNotBlank())
    }
}
