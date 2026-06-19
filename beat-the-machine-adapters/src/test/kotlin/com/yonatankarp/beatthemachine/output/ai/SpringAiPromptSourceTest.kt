package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SpringAiPromptSourceTest {
    private val fallback = mockk<PromptSource>()

    @Test
    fun `returns a valid phrase within the difficulty band`() =
        runTest {
            val llm = LlmText { _, _ -> "  dragon eating a cookie  " }
            val source = SpringAiPromptSource(llm, fallback)
            assertEquals(Prompt("dragon eating a cookie"), source next Difficulty.HARD)
        }

    @Test
    fun `retries past invalid output then succeeds`() =
        runTest {
            var calls = 0
            val llm = LlmText { _, _ -> if (calls++ == 0) "" else "ocean wave" }
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 3)
            assertEquals(Prompt("ocean wave"), source next Difficulty.EASY)
        }

    @Test
    fun `falls back when the model keeps failing`() =
        runTest {
            val llm = LlmText { _, _ -> throw RuntimeException("model down") }
            coEvery { fallback next Difficulty.MEDIUM } returns Prompt("dolphin on fire")
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
            assertEquals(Prompt("dolphin on fire"), source next Difficulty.MEDIUM)
        }

    @Test
    fun `falls back when output never fits the band`() =
        runTest {
            val llm = LlmText { _, _ -> "this phrase is far too many words to be easy" }
            coEvery { fallback next Difficulty.EASY } returns Prompt("red car")
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
            assertEquals(Prompt("red car"), source next Difficulty.EASY)
        }
}
