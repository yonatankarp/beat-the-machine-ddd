package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

val SpringAiPromptSourceSuite by testSuite {
    given("a prompt source backed by Spring AI") {
        whenever("the model returns a phrase within the difficulty band") {
            then("it returns the phrase trimmed to the band") {
                val fallback = mockk<PromptSource>()
                val llm = LlmText { _, _ -> "  dragon eating a cookie  " }
                val source = SpringAiPromptSource(llm, fallback)
                val result = source answer PromptSource.Query(Difficulty.HARD)
                result shouldBe Prompt("dragon eating a cookie")
            }
        }

        whenever("the model returns empty output before an in-band phrase") {
            then("it retries past the empty output then returns the first in-band phrase") {
                val fallback = mockk<PromptSource>()
                var calls = 0
                val llm = LlmText { _, _ -> if (calls++ == 0) "" else "ocean wave" }
                val source = SpringAiPromptSource(llm, fallback, maxAttempts = 3)
                val result = source answer PromptSource.Query(Difficulty.EASY)
                result shouldBe Prompt("ocean wave")
            }
        }

        whenever("the model keeps throwing") {
            then("it falls back to the seed source") {
                val fallback = mockk<PromptSource>()
                val llm = LlmText { _, _ -> throw RuntimeException("model down") }
                coEvery { fallback answer PromptSource.Query(Difficulty.MEDIUM) } returns Prompt("dolphin on fire")
                val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
                val result = source answer PromptSource.Query(Difficulty.MEDIUM)
                result shouldBe Prompt("dolphin on fire")
            }
        }

        whenever("the model output never fits the difficulty band") {
            then("it falls back to the seed source") {
                val fallback = mockk<PromptSource>()
                val llm = LlmText { _, _ -> "this phrase is far too many words to be easy" }
                coEvery { fallback answer PromptSource.Query(Difficulty.EASY) } returns Prompt("red car")
                val source = SpringAiPromptSource(llm, fallback, maxAttempts = 2)
                val result = source answer PromptSource.Query(Difficulty.EASY)
                result shouldBe Prompt("red car")
            }
        }
    }
}
