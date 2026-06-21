package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

val SpringAiPromptSourceSuite by testSuite {
    test("returns a valid phrase trimmed to the difficulty band") {
        val fallback = mockk<PromptSource>()
        val llm = LlmText { _, _ -> "  dragon eating a cookie  " }
        val source = SpringAiPromptSource(llm, fallback)
        val result = source answer PromptSource.Query(Difficulty.HARD)
        result shouldBe Prompt("dragon eating a cookie")
    }

    test("retries past empty output then returns first in-band phrase") {
        val fallback = mockk<PromptSource>()
        var calls = 0
        val llm = LlmText { _, _ -> if (calls++ == 0) "" else "ocean wave" }
        val source = SpringAiPromptSource(llm, fallback, maxAttempts = 3)
        val result = source answer PromptSource.Query(Difficulty.EASY)
        result shouldBe Prompt("ocean wave")
    }

    test("falls back to seed source when the model keeps throwing") {
        val fallback = mockk<PromptSource>()
        val llm = LlmText { _, _ -> throw RuntimeException("model down") }
        coEvery { fallback answer PromptSource.Query(Difficulty.MEDIUM) } returns Prompt("dolphin on fire")
        val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
        val result = source answer PromptSource.Query(Difficulty.MEDIUM)
        result shouldBe Prompt("dolphin on fire")
    }

    test("falls back when output never fits the difficulty band") {
        val fallback = mockk<PromptSource>()
        val llm = LlmText { _, _ -> "this phrase is far too many words to be easy" }
        coEvery { fallback answer PromptSource.Query(Difficulty.EASY) } returns Prompt("red car")
        val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
        val result = source answer PromptSource.Query(Difficulty.EASY)
        result shouldBe Prompt("red car")
    }
}
