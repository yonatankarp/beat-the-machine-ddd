package com.yonatankarp.beatthemachine.domain.game

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttemptNumberTest {
    @Test
    fun `should create attempt number with valid positive value`() {
        // Given
        val number = 1

        // When
        val attemptNumber = AttemptNumber(number)

        // Then
        attemptNumber.value shouldBe 1
    }

    @Test
    fun `should allow zero as initial attempt number`() {
        // Given
        val number = 0

        // When
        val attemptNumber = AttemptNumber(number)

        // Then
        attemptNumber.value shouldBe 0
    }

    @Test
    fun `should throw exception when attempt number is negative`() {
        // Given
        val number = -1

        // When / Then
        shouldThrow<IllegalArgumentException> {
            AttemptNumber(number)
        }
    }

    @Test
    fun `should increment to next attempt number`() {
        // Given
        val attemptNumber = AttemptNumber(1)

        // When
        val nextAttempt = attemptNumber.increment()

        // Then
        nextAttempt.value shouldBe 2
    }

    @Test
    fun `should access value property directly`() {
        // Given
        val attemptNumber = AttemptNumber(42)

        // When
        val value = attemptNumber.value

        // Then
        value shouldBe 42
    }

    @Test
    fun `should access value property for comparison`() {
        // Given
        val attemptNumber = AttemptNumber(5)

        // When & Then
        (attemptNumber.value > 0) shouldBe true
        (attemptNumber.value < 10) shouldBe true
        (attemptNumber.value == 5) shouldBe true
    }

    @Test
    fun `should access value property for arithmetic operations`() {
        // Given
        val attemptNumber = AttemptNumber(3)

        // When
        val doubled = attemptNumber.value * 2
        val sum = attemptNumber.value + 1

        // Then
        doubled shouldBe 6
        sum shouldBe 4
    }

    @Test
    fun `should invoke getValue method when used as parameter`() {
        // Given
        val attemptNumber = AttemptNumber(7)

        // When
        val result = processAttemptNumber(attemptNumber)

        // Then
        result shouldBe "Attempt: 7"
    }

    private fun processAttemptNumber(attempt: AttemptNumber): String = "Attempt: ${attempt.value}"
}
