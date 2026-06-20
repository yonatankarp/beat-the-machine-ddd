package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GuessTest {
    @Test
    fun `rejects blank word`() {
        // Given
        val word = "   "

        // When / Then
        assertFailsWith<InvalidGuess> { Guess(word) }
    }

    @Test
    fun `rejects empty word`() {
        // Given
        val word = ""

        // When / Then
        assertFailsWith<InvalidGuess> { Guess(word) }
    }

    @Test
    fun `normalized trims and lowercases`() {
        // Given
        val guess = Guess("Hello")

        // When
        val normalized = guess.normalized()

        // Then
        assertEquals("hello", normalized)
    }
}
