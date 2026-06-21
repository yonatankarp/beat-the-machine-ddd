package com.yonatankarp.beatthemachine.domain.valueobject

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val LivesSuite by testSuite {
    test("cannot be negative") {
        // Given
        val count = -1

        // When / Then
        shouldThrow<IllegalArgumentException> { Lives(count) }
    }

    test("lose decrements and floors at zero") {
        // Given
        val oneLife = Lives(1)
        val noLives = Lives(0)

        // When
        val afterLosingFromOne = oneLife.lose()
        val afterLosingFromZero = noLives.lose()

        // Then
        afterLosingFromOne shouldBe Lives(0)
        afterLosingFromZero shouldBe Lives(0)
    }

    test("is exhausted at zero") {
        // Given
        val noLives = Lives(0)
        val oneLife = Lives(1)

        // When
        val exhausted = noLives.isExhausted()
        val notExhausted = oneLife.isExhausted()

        // Then
        exhausted.shouldBeTrue()
        notExhausted.shouldBeFalse()
    }

    test("initial lives are granted per difficulty") {
        // When
        val easy = Lives.initialFor(Difficulty.EASY)
        val medium = Lives.initialFor(Difficulty.MEDIUM)
        val hard = Lives.initialFor(Difficulty.HARD)

        // Then
        easy shouldBe Lives(8)
        medium shouldBe Lives(6)
        hard shouldBe Lives(4)
    }
}
