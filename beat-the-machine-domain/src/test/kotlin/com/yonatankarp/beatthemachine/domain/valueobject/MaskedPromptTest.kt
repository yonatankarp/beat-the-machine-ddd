package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val MaskedPromptSuite by testSuite {
    test("hides every word when there are no guesses") {
        // Given
        val secret = "hello world".asPrompt()
        val guesses = emptySet<Guess>()

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        masked.tokens shouldBe listOf(MaskedToken.Hidden(5), MaskedToken.Hidden(5))
        masked.isFullyRevealed().shouldBeFalse()
    }

    test("reveals a matching word case-insensitively") {
        // Given
        val secret = "Hello World".asPrompt()
        val guesses = setOf("hello".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        masked.tokens[0] shouldBe MaskedToken.Revealed("Hello")
        masked.tokens[1] shouldBe MaskedToken.Hidden(5)
    }

    test("reveals every occurrence of a repeated word") {
        // Given
        val secret = "na na batman".asPrompt()
        val guesses = setOf("na".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        masked.tokens shouldBe listOf(MaskedToken.Revealed("na"), MaskedToken.Revealed("na"), MaskedToken.Hidden(6))
    }

    test("collapses arbitrary whitespace using one rule") {
        // Given
        val secret = "hello\t \nworld".asPrompt()
        val guesses = setOf("world".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        masked.tokens.size shouldBe 2
        masked.tokens[1] shouldBe MaskedToken.Revealed("world")
    }

    test("is fully revealed when all words are guessed") {
        // Given
        val secret = "hello world".asPrompt()
        val guesses = setOf("hello".asGuess(), "world".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        masked.isFullyRevealed().shouldBeTrue()
    }
}
