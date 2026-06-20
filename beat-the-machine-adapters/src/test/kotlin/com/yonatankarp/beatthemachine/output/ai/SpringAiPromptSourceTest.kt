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
    fun `returns a valid phrase trimmed to the difficulty band`() =
        runTest {
            // Given
            val llm = LlmText { _, _ -> "  dragon eating a cookie  " }
            val source = SpringAiPromptSource(llm, fallback)

            // When
            val result = source answer PromptSource.Query(Difficulty.HARD)

            // Then
            assertEquals(Prompt("dragon eating a cookie"), result)
        }

    @Test
    fun `retries past empty output then returns first in-band phrase`() =
        runTest {
            // Given
            var calls = 0
            val llm = LlmText { _, _ -> if (calls++ == 0) "" else "ocean wave" }
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 3)

            // When
            val result = source answer PromptSource.Query(Difficulty.EASY)

            // Then
            assertEquals(Prompt("ocean wave"), result)
        }

    @Test
    fun `falls back to seed source when the model keeps throwing`() =
        runTest {
            // Given
            val llm = LlmText { _, _ -> throw RuntimeException("model down") }
            coEvery { fallback answer PromptSource.Query(Difficulty.MEDIUM) } returns Prompt("dolphin on fire")
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)

            // When
            val result = source answer PromptSource.Query(Difficulty.MEDIUM)

            // Then
            assertEquals(Prompt("dolphin on fire"), result)
        }

    @Test
    fun `falls back when output never fits the difficulty band`() =
        runTest {
            // Given
            val llm = LlmText { _, _ -> "this phrase is far too many words to be easy" }
            coEvery { fallback answer PromptSource.Query(Difficulty.EASY) } returns Prompt("red car")
            val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)

            // When
            val result = source answer PromptSource.Query(Difficulty.EASY)

            // Then
            assertEquals(Prompt("red car"), result)
        }
}
