package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LivesTest {
    @Test
    fun `cannot be negative`() {
        // Given
        val count = -1

        // When / Then
        assertFailsWith<IllegalArgumentException> { Lives(count) }
    }

    @Test
    fun `lose decrements and floors at zero`() {
        // Given
        val oneLife = Lives(1)
        val noLives = Lives(0)

        // When
        val afterLosingFromOne = oneLife.lose()
        val afterLosingFromZero = noLives.lose()

        // Then
        assertEquals(Lives(0), afterLosingFromOne)
        assertEquals(Lives(0), afterLosingFromZero)
    }

    @Test
    fun `is exhausted at zero`() {
        // Given
        val noLives = Lives(0)
        val oneLife = Lives(1)

        // When
        val exhausted = noLives.isExhausted()
        val notExhausted = oneLife.isExhausted()

        // Then
        assertTrue(exhausted)
        assertFalse(notExhausted)
    }

    @Test
    fun `initial lives are granted per difficulty`() {
        // When
        val easy = Lives.initialFor(Difficulty.EASY)
        val medium = Lives.initialFor(Difficulty.MEDIUM)
        val hard = Lives.initialFor(Difficulty.HARD)

        // Then
        assertEquals(Lives(8), easy)
        assertEquals(Lives(6), medium)
        assertEquals(Lives(4), hard)
    }
}
