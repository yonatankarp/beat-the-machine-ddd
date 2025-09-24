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
        history.isEmpty shouldBe true
    }

    @Test
    fun `should create empty attempt history using factory method`() {
        // Given / When
        val history = AttemptHistory.empty()

        // Then
        history.isEmpty shouldBe true
    }

    @Test
    fun `should add guess to history`() {
        // Given
        val history = AttemptHistory()
        val guess = Guess.of("test")

        // When
        val updatedHistory = history.addGuess(guess)

        // Then
        updatedHistory.isEmpty shouldBe false
    }

    @Test
    fun `should return attempt number based on history count`() {
        // Given
        val history =
            AttemptHistory()
                .addGuess(Guess.of("first"))
                .addGuess(Guess.of("second"))

        // When
        val attemptNumber = history.count

        // Then
        attemptNumber shouldBe AttemptNumber(2)
    }

    @Test
    fun `should reject zero as max attempts value`() {
        // Given / When
        val result = runCatching { MaxAttempts(0) }

        // Then
        result.isFailure shouldBe true
    }

    @Test
    fun `should return false when attempt number exactly equals max attempts`() {
        // Given
        val maxAttempts = MaxAttempts(5)
        val attemptNumber = AttemptNumber(5)

        // When
        val exceeded = maxAttempts.isExceeded(attemptNumber)

        // Then
        exceeded shouldBe false
    }
}
