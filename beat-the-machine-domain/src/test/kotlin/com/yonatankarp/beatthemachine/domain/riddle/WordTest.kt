package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.fixtures.WordFixtures
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WordTest {
    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle single letter word`() {
            // Given
            val singleLetter = WordFixtures.EdgeCases.singleLetter()

            // When & Then
            singleLetter.value shouldBe "a"
        }

        @Test
        fun `should handle long word`() {
            // Given
            val longWord = WordFixtures.EdgeCases.longWord()

            // When & Then
            longWord.value shouldBe "extraordinary"
        }
    }

    @Nested
    inner class Normalization {
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
        fun `should normalize word with mixed case`() {
            // Given
            val mixedCaseText = "SuNsEt"

            // When
            val word = Word(mixedCaseText)

            // Then
            word.value shouldBe "sunset"
        }

        @Test
        fun `should ensure normalized value is never null or empty`() {
            // Given
            val text = "WORD"

            // When
            val word = Word(text)

            // Then
            word.value shouldNotBe null
            word.value.isNotBlank() shouldBe true
            word.value shouldBe "word"
        }

        @Test
        fun `should handle unicode characters in normalization`() {
            // Given
            val unicodeText = "CAFÉ"

            // When
            val word = Word(unicodeText)

            // Then
            word.value shouldNotBe null
            word.value shouldBe "café"
            word.value.length shouldBe unicodeText.length
        }
    }

    @Nested
    inner class Obfuscation {
        @Test
        fun `should return obfuscated version of word`() {
            // Given
            val word = WordFixtures.Simple.hello()

            // When
            val obfuscated = word.obfuscated

            // Then
            obfuscated shouldBe "-----"
        }

        @Test
        fun `should obfuscate different length words correctly`() {
            // Given
            val shortWord = WordFixtures.EdgeCases.singleLetter()
            val longWord = WordFixtures.EdgeCases.longWord()

            // When & Then
            shortWord.obfuscated shouldBe "-"
            longWord.obfuscated shouldBe "-------------"
        }
    }

    @Nested
    inner class Creation {
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
    }
}
