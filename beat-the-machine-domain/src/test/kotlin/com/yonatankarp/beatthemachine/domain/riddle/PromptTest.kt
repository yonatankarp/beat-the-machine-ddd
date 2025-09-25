package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.fixtures.PromptFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.WordFixtures
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PromptTest {
    @Nested
    inner class Contains {
        @Test
        fun `should contain word when word is in prompt`() {
            // Given
            val prompt = PromptFixtures.Photography.goldenSunsetBeach()
            val searchWord = WordFixtures.Nature.sunset()

            // When
            val contains = prompt contains searchWord

            // Then
            contains shouldBe true
        }

        @Test
        fun `should not contain word when word is not in prompt`() {
            // Given
            val prompt = PromptFixtures.Photography.goldenSunsetBeach()
            val searchWord = WordFixtures.Art.abstract()

            // When
            val contains = prompt contains searchWord

            // Then
            contains shouldBe false
        }
    }

    @Nested
    inner class Construction {
        @Test
        fun `should create prompt from varargs of strings`() {
            // Given
            val expectedWords = listOf("golden", "sunset", "beach")

            // When
            val prompt = Prompt.of("golden", "sunset", "beach")

            // Then
            expectedWords.forEach { word ->
                (prompt contains Word(word)) shouldBe true
            }
        }

        @Test
        fun `should create prompt from string with spaces`() {
            // Given
            val expectedWords = listOf("crystal", "clear", "lake")

            // When
            val prompt = Prompt.from("crystal clear lake")

            // Then
            expectedWords.forEach { word ->
                (prompt contains Word(word)) shouldBe true
            }
        }

        @Test
        fun `should create complex prompt with many words`() {
            // Given
            val prompt = PromptFixtures.Complex.longPrompt()

            // When & Then
            prompt.words.size shouldBe 9
            (prompt contains Word("magnificent")) shouldBe true
            (prompt contains Word("moonlight")) shouldBe true
        }
    }

    @Nested
    inner class Validation {
        @Test
        fun `should throw IllegalArgumentException when creating prompt with empty list`() {
            // Given
            val emptyWordList = emptyList<Word>()

            // When/Then
            shouldThrow<IllegalArgumentException> {
                Prompt(emptyWordList)
            }
        }

        @Test
        fun `should accept single word prompt`() {
            // Given
            val singleWordPrompt = PromptFixtures.Complex.singleWord()

            // When & Then
            singleWordPrompt.words.size shouldBe 1
            (singleWordPrompt contains Word("serenity")) shouldBe true
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle prompt with duplicate words`() {
            // Given
            val prompt = PromptFixtures.EdgeCases.duplicateWords()

            // When & Then
            (prompt contains Word("blue")) shouldBe true
            (prompt contains Word("sky")) shouldBe true
            (prompt contains Word("ocean")) shouldBe true
        }

        @Test
        fun `should handle prompt with all same words`() {
            // Given
            val prompt = PromptFixtures.EdgeCases.allSameWord()

            // When & Then
            (prompt contains Word("test")) shouldBe true
            prompt.words.size shouldBe 3
        }
    }
}
