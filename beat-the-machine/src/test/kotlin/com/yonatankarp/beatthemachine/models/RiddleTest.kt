package com.yonatankarp.beatthemachine.models

import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.MISS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RiddleTest {
    @Test
    fun `giveUp reveals every word of the prompt`() {
        // Given
        val riddle = Riddle(id = 10, startPrompt = "--- --- ---", prompt = "dog eat god", url = "a nice url")

        // When
        val actual = riddle.giveUp()

        // Then
        // The legacy implementation reveals each word (first element) but incorrectly
        // marks every result as MISS instead of a loss/reveal state. The correct
        // loss semantics (forfeit reveals the full prompt and scores as a loss) are
        // implemented in Task 2.3 via Challenge.forfeit().
        assertEquals(listOf("dog", "eat", "god"), actual.map { it.first })
    }

    @Test
    fun `should init prompt with missed guesses`() {
        // Given
        val riddle = Riddle(id = 10, startPrompt = "--- --- ---", prompt = "dog eat god", url = "a nice url")

        // When
        val actual = riddle.initPrompt()

        // Then
        actual.forEach {
            assertEquals("---", it.first)
            assertEquals(MISS, it.second)
        }
    }
}
