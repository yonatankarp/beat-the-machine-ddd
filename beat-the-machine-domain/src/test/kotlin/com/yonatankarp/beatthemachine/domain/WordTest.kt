package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class WordTest {
    @Test
    fun `masked returns the correct number of dashes`() {
        // Given
        val word = Word.of("example")

        // When
        val masked = word.masked()

        // Then
        assertEquals(masked, "-------")
    }

    @Test
    fun `of trims and lowercases the word`() {
        // Given
        val raw = "  ExAmPlE  "

        // When
        val word = Word.of(raw)

        // Then
        assertEquals(word.value, "example")
    }

    @Test
    fun `should throw an error if multiple words are given`(){
        // Given
        val raw = "two words"

        // When / Then
        assertThrows<IllegalArgumentException> {
            Word.of(raw)
        }
    }
}
