package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptTest {
    @Test
    fun `splits on single space`() {
        // Given
        val prompt = Prompt("hello world")

        // When
        val words = prompt.words()

        // Then
        assertEquals(listOf("hello", "world"), words)
    }

    @Test
    fun `splits on multiple whitespace characters`() {
        // Given
        val prompt = Prompt("a\t b\n c")

        // When
        val words = prompt.words()

        // Then
        assertEquals(listOf("a", "b", "c"), words)
    }

    @Test
    fun `rejects blank text`() {
        // Given
        val text = "   "

        // When / Then
        assertFailsWith<IllegalArgumentException> { Prompt(text) }
    }

    @Test
    fun `rejects empty text`() {
        // Given
        val text = ""

        // When / Then
        assertFailsWith<IllegalArgumentException> { Prompt(text) }
    }
}
