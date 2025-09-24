package com.yonatankarp.beatthemachine.domain.riddle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GuessTest {
    @Test
    fun `should create guess from list of words`() {
        // Given
        val words = listOf(Word("beautiful"), Word("sunset"))

        // When
        val guess = Guess(words)

        // Then
        guess.words shouldBe words
    }

    @Test
    fun `should create guess from space-separated text`() {
        // Given
        val text = "beautiful sunset mountain"

        // When
        val guess = Guess.from(text)

        // Then
        guess.words shouldBe listOf(Word("beautiful"), Word("sunset"), Word("mountain"))
    }

    @Test
    fun `should throw exception when creating guess with empty list`() {
        // Given
        val emptyWords = emptyList<Word>()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Guess(emptyWords)
        }
    }
}
