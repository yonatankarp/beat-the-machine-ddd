package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.riddle.Guess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttemptHistoryTest {
    @Test
    fun `should create empty attempt history`() {
        // Given / When
        val history = AttemptHistory()

        // Then
        history.isEmpty() shouldBe true
    }

    @Test
    fun `should add guess to history`() {
        // Given
        val history = AttemptHistory()
        val guess = Guess.of("test")

        // When
        val updatedHistory = history.addGuess(guess)

        // Then
        updatedHistory.isEmpty() shouldBe false
    }

    @Test
    fun `should return attempt number based on history count`() {
        // Given
        val history =
            AttemptHistory()
                .addGuess(Guess.of("first"))
                .addGuess(Guess.of("second"))

        // When
        val attemptNumber = history.count()

        // Then
        attemptNumber shouldBe AttemptNumber(2)
    }
}
