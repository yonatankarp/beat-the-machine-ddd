package com.yonatankarp.beatthemachine.domain.game

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MaxAttemptsTest {
    @Test
    fun `should create max attempts with valid positive value`() {
        // Given
        val limit = 5

        // When
        val maxAttempts = MaxAttempts(limit)

        // Then
        maxAttempts.value shouldBe 5
    }

    @Test
    fun `should return true when attempt number exceeds max attempts`() {
        // Given
        val maxAttempts = MaxAttempts(3)
        val attemptNumber = AttemptNumber(4)

        // When
        val isExceeded = maxAttempts.isExceeded(attemptNumber)

        // Then
        isExceeded shouldBe true
    }

    @Test
    fun `should return false when attempt number does not exceed max attempts`() {
        // Given
        val maxAttempts = MaxAttempts(5)
        val attemptNumber = AttemptNumber(3)

        // When
        val isExceeded = maxAttempts.isExceeded(attemptNumber)

        // Then
        isExceeded shouldBe false
    }
}
