package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.test.dsl.asPrompt
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
    fun `forSecret scales lives by word count and difficulty`() {
        // Given
        val twoWordPrompt = "hello world".asPrompt()

        // When
        val easy = Lives.forSecret(twoWordPrompt, Difficulty.EASY)
        val medium = Lives.forSecret(twoWordPrompt, Difficulty.MEDIUM)
        val hard = Lives.forSecret(twoWordPrompt, Difficulty.HARD)

        // Then
        assertEquals(Lives(9), easy)
        assertEquals(Lives(6), medium)
        assertEquals(Lives(4), hard)
    }

    @Test
    fun `forSecret floors at MIN_LIVES for very short secrets`() {
        // Given
        val oneWordPrompt = "x".asPrompt()

        // When
        val result = Lives.forSecret(oneWordPrompt, Difficulty.HARD)

        // Then
        assertEquals(Lives(2), result) // round(3 * 1 * 0.7) = 2 >= MIN_LIVES
    }
}
