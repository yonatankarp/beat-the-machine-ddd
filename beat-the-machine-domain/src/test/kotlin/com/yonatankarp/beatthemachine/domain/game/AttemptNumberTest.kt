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
}
