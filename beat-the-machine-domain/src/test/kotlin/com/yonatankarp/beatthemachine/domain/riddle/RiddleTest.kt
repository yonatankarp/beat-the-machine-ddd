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
        result.feedbacks[0].status shouldBe WordFeedback.Status.CORRECT_POSITION
        result.feedbacks[1].status shouldBe WordFeedback.Status.CORRECT_POSITION
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
        result.feedbacks[0].status shouldBe WordFeedback.Status.CORRECT_POSITION
        result.feedbacks[1].status shouldBe WordFeedback.Status.WRONG_POSITION
        result.feedbacks[2].status shouldBe WordFeedback.Status.CORRECT_POSITION
        result.feedbacks[3].status shouldBe WordFeedback.Status.WRONG_WORD
    }
}
