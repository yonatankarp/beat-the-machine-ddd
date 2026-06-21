package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val GuessSuite by testSuite {
    test("rejects blank word") {
        // Given
        val word = "   "

        // When / Then
        shouldThrow<InvalidGuess> { Guess(word) }
    }

    test("rejects empty word") {
        // Given
        val word = ""

        // When / Then
        shouldThrow<InvalidGuess> { Guess(word) }
    }

    test("normalized trims and lowercases") {
        // Given
        val guess = Guess("Hello")

        // When
        val normalized = guess.normalized()

        // Then
        normalized shouldBe "hello"
    }
}
