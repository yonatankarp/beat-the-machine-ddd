package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaskedPromptTest {
    @Test
    fun `hides every word when there are no guesses`() {
        // Given
        val secret = "hello world".asPrompt()
        val guesses = emptySet<Guess>()

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        assertEquals(listOf(MaskedToken.Hidden(5), MaskedToken.Hidden(5)), masked.tokens)
        assertFalse(masked.isFullyRevealed())
    }

    @Test
    fun `reveals a matching word case-insensitively`() {
        // Given
        val secret = "Hello World".asPrompt()
        val guesses = setOf("hello".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        assertEquals(MaskedToken.Revealed("Hello"), masked.tokens[0])
        assertEquals(MaskedToken.Hidden(5), masked.tokens[1])
    }

    @Test
    fun `reveals every occurrence of a repeated word`() {
        // Given
        val secret = "na na batman".asPrompt()
        val guesses = setOf("na".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        assertEquals(
            listOf(MaskedToken.Revealed("na"), MaskedToken.Revealed("na"), MaskedToken.Hidden(6)),
            masked.tokens,
        )
    }

    @Test
    fun `collapses arbitrary whitespace using one rule`() {
        // Given
        val secret = "hello\t \nworld".asPrompt()
        val guesses = setOf("world".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        assertEquals(2, masked.tokens.size)
        assertEquals(MaskedToken.Revealed("world"), masked.tokens[1])
    }

    @Test
    fun `is fully revealed when all words are guessed`() {
        // Given
        val secret = "hello world".asPrompt()
        val guesses = setOf("hello".asGuess(), "world".asGuess())

        // When
        val masked = MaskedPrompt.of(secret, guesses)

        // Then
        assertTrue(masked.isFullyRevealed())
    }
}
