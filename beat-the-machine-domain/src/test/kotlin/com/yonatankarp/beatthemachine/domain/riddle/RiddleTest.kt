package com.yonatankarp.beatthemachine.domain.riddle

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RiddleTest {
    @Test
    fun `should create riddle and evaluate guess returning result`() {
        // Given
        val prompt = Prompt.of("beautiful", "sunset")
        val imageUrl = ImageUrl("https://example.com/sunset.jpg")
        val guess = Guess.of("beautiful", "sunset")

        // When
        val riddle = Riddle(prompt, imageUrl)
        val result = riddle.evaluate(guess)

        // Then
        result.feedbacks.size shouldBe 2
        result.isAllCorrect shouldBe true
    }

    @Test
    fun `should evaluate guess with mixed feedback statuses`() {
        // Given
        val prompt = Prompt.of("I", "love", "my", "work")
        val imageUrl = ImageUrl("https://example.com/image.jpg")
        val guess = Guess.of("I", "work", "my", "life")

        // When
        val riddle = Riddle(prompt, imageUrl)
        val result = riddle.evaluate(guess)

        // Then
        result.feedbacks.size shouldBe 4
        result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        result[1] shouldBe WordFeedback.Status.WRONG_POSITION
        result[2] shouldBe WordFeedback.Status.CORRECT_POSITION
        result[3] shouldBe WordFeedback.Status.WRONG_WORD
    }

    @Test
    fun `should return wrong word when guess index exceeds prompt size`() {
        // Given
        val prompt = Prompt.of("word1", "word2")
        val imageUrl = ImageUrl("https://example.com/image.jpg")
        val guess = Guess.of("word1", "word2", "extra")

        // When
        val riddle = Riddle(prompt, imageUrl)
        val result = riddle.evaluate(guess)

        // Then
        result[2] shouldBe WordFeedback.Status.WRONG_WORD
    }

    @Test
    fun `should expose prompt through property`() {
        // Given
        val prompt = Prompt.of("test")
        val imageUrl = ImageUrl("https://example.com/image.jpg")
        val riddle = Riddle(prompt, imageUrl)

        // When & Then
        riddle.prompt shouldBe prompt
    }

    @Test
    fun `should expose imageUrl through property`() {
        // Given
        val prompt = Prompt.of("test")
        val imageUrl = ImageUrl("https://example.com/image.jpg")
        val riddle = Riddle(prompt, imageUrl)

        // When & Then
        riddle.imageUrl shouldBe imageUrl
    }

    @Test
    fun `should return true when all words are in correct position`() {
        // Given
        val prompt = Prompt.of("beautiful", "sunset")
        val imageUrl = ImageUrl("https://example.com/sunset.jpg")
        val guess = Guess.of("beautiful", "sunset")
        val riddle = Riddle(prompt, imageUrl)

        // When
        val result = riddle.evaluate(guess)

        // Then
        result.isAllCorrect shouldBe true
    }

    @Test
    fun `should access feedback status by index using operator`() {
        // Given
        val prompt = Prompt.of("I", "love", "my", "work")
        val imageUrl = ImageUrl("https://example.com/image.jpg")
        val guess = Guess.of("I", "work", "my", "life")
        val riddle = Riddle(prompt, imageUrl)

        // When
        val result = riddle.evaluate(guess)

        // Then
        result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
    }
}
