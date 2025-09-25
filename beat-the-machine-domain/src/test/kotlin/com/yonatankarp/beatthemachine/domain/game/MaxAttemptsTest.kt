package com.yonatankarp.beatthemachine.domain.game

import io.kotest.assertions.throwables.shouldThrow
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

    @Test
    fun `should throw exception when max attempts is zero or negative`() {
        // Given / When / Then
        shouldThrow<IllegalArgumentException> {
            MaxAttempts(0)
        }

        shouldThrow<IllegalArgumentException> {
            MaxAttempts(-1)
        }
    }

    @Test
    fun `should return false when attempt number equals max attempts`() {
        // Given
        val maxAttempts = MaxAttempts(3)
        val attemptNumber = AttemptNumber(3)

        // When
        val isExceeded = maxAttempts.isExceeded(attemptNumber)

        // Then
        isExceeded shouldBe false
    }

    @Test
    fun `should access value property directly`() {
        // Given
        val maxAttempts = MaxAttempts(10)

        // When
        val value = maxAttempts.value

        // Then
        value shouldBe 10
    }

    @Test
    fun `should access value property for comparison operations`() {
        // Given
        val maxAttempts = MaxAttempts(5)

        // When & Then
        (maxAttempts.value > 0) shouldBe true
        (maxAttempts.value < 10) shouldBe true
        (maxAttempts.value == 5) shouldBe true
        (maxAttempts.value != 3) shouldBe true
    }

    @Test
    fun `should access value property for arithmetic operations`() {
        // Given
        val maxAttempts = MaxAttempts(8)

        // When
        val half = maxAttempts.value / 2
        val increased = maxAttempts.value + 2

        // Then
        half shouldBe 4
        increased shouldBe 10
    }

    @Test
    fun `should invoke getValue method when used as parameter`() {
        // Given
        val maxAttempts = MaxAttempts(12)

        // When
        val result = processMaxAttempts(maxAttempts)

        // Then
        result shouldBe "Max: 12"
    }

    private fun processMaxAttempts(max: MaxAttempts): String = "Max: ${max.value}"
}
