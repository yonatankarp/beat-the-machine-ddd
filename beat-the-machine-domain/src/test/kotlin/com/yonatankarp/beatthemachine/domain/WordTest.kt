package com.yonatankarp.beatthemachine.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WordTest {
    @Test
    fun `should create word with valid text`() {
        // Given
        val text = "hello"

        // When
        val word = Word(text)

        // Then
        word.value shouldBe "hello"
    }

    @Test
    fun `should throw exception when creating word with spaces`() {
        // Given
        val textWithSpaces = "hello world"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Word(textWithSpaces)
        }
    }

    @Test
    fun `should normalize word to lowercase`() {
        // Given
        val uppercaseText = "HELLO"

        // When
        val word = Word(uppercaseText)

        // Then
        word.value shouldBe "hello"
    }

    @Test
    fun `should return obfuscated version of word`() {
        // Given
        val word = Word("hello")

        // When
        val obfuscated = word.obfuscated()

        // Then
        obfuscated shouldBe "-----"
    }
}
