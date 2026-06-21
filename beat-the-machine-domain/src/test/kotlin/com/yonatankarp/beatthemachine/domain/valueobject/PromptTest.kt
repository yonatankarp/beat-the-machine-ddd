package com.yonatankarp.beatthemachine.domain.valueobject

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val PromptSuite by testSuite {
    test("splits on single space") {
        // Given
        val prompt = Prompt("hello world")

        // When
        val words = prompt.words()

        // Then
        words shouldBe listOf("hello", "world")
    }

    test("splits on multiple whitespace characters") {
        // Given
        val prompt = Prompt("a\t b\n c")

        // When
        val words = prompt.words()

        // Then
        words shouldBe listOf("a", "b", "c")
    }

    test("rejects blank text") {
        // Given
        val text = "   "

        // When / Then
        shouldThrow<IllegalArgumentException> { Prompt(text) }
    }

    test("rejects empty text") {
        // Given
        val text = ""

        // When / Then
        shouldThrow<IllegalArgumentException> { Prompt(text) }
    }
}
