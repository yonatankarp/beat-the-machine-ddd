package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaskedPromptTest {
    private fun prompt(t: String) = Prompt(t)

    @Test
    fun `hides every word when there are no guesses`() {
        val masked = MaskedPrompt.of(prompt("hello world"), emptySet())
        assertEquals(listOf(MaskedToken.Hidden, MaskedToken.Hidden), masked.tokens)
        assertFalse(masked.isFullyRevealed())
    }

    @Test
    fun `reveals a matching word case-insensitively`() {
        val masked = MaskedPrompt.of(prompt("Hello World"), setOf(Guess("hello")))
        assertEquals(MaskedToken.Revealed("Hello"), masked.tokens[0])
        assertEquals(MaskedToken.Hidden, masked.tokens[1])
    }

    @Test
    fun `reveals every occurrence of a repeated word`() {
        val masked = MaskedPrompt.of(prompt("na na batman"), setOf(Guess("na")))
        assertEquals(
            listOf(MaskedToken.Revealed("na"), MaskedToken.Revealed("na"), MaskedToken.Hidden),
            masked.tokens,
        )
    }

    @Test
    fun `collapses arbitrary whitespace using one rule`() {
        val masked = MaskedPrompt.of(prompt("hello\t \nworld"), setOf(Guess("world")))
        assertEquals(2, masked.tokens.size)
        assertEquals(MaskedToken.Revealed("world"), masked.tokens[1])
    }

    @Test
    fun `is fully revealed when all words are guessed`() {
        val masked = MaskedPrompt.of(prompt("hello world"), setOf(Guess("hello"), Guess("world")))
        assertTrue(masked.isFullyRevealed())
    }
}
