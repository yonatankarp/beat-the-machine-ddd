package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.Word
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PromptTest {
    @Test
    fun `should contain word when word is in prompt`() {
        // Given
        val words = listOf(Word("beautiful"), Word("sunset"), Word("over"), Word("mountains"))
        val prompt = Prompt(words)
        val searchWord = Word("sunset")

        // When
        val contains = prompt contains searchWord

        // Then
        contains shouldBe true
    }

    @Test
    fun `should create prompt from varargs of strings`() {
        // Given
        val expectedWords = listOf("beautiful", "sunset", "mountains")

        // When
        val prompt = Prompt.of("beautiful", "sunset", "mountains")

        // Then
        expectedWords.forEach { word ->
            (prompt contains Word(word)) shouldBe true
        }
    }

    @Test
    fun `should create prompt from string with spaces`() {
        // Given
        val expectedWords = listOf("beautiful", "sunset", "over", "mountains")

        // When
        val prompt = Prompt.from("beautiful sunset over mountains")

        // Then
        expectedWords.forEach { word ->
            (prompt contains Word(word)) shouldBe true
        }
    }

    @Test
    fun `should throw IllegalArgumentException when creating prompt with empty list`() {
        // Given
        val emptyWordList = emptyList<Word>()

        // When/Then
        shouldThrow<IllegalArgumentException> {
            Prompt(emptyWordList)
        }
    }
}
