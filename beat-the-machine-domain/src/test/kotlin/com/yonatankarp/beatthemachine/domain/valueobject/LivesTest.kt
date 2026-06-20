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
    fun `lives scale with word count and difficulty multiplier`() {
        // Given
        val easyPrompt = Prompt("dragon cookie")
        val mediumPrompt = Prompt("a b c")
        val hardPrompt = Prompt("a b c d")

        // When
        val easyLives = Lives.forSecret(easyPrompt, Difficulty.EASY)
        val mediumLives = Lives.forSecret(mediumPrompt, Difficulty.MEDIUM)
        val hardLives = Lives.forSecret(hardPrompt, Difficulty.HARD)

        // Then
        assertEquals(Lives(9), easyLives)
        assertEquals(Lives(9), mediumLives)
        assertEquals(Lives(8), hardLives)
    }

    @Test
    fun `lives never drop below the floor`() {
        // Given
        val prompt = Prompt("cat")

        // When
        val lives = Lives.forSecret(prompt, Difficulty.HARD)

        // Then
        assertEquals(Lives(2), lives)
    }
}
