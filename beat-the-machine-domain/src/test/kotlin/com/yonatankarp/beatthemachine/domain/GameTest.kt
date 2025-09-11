package com.yonatankarp.beatthemachine.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GameTest {
    @Test
    fun `apply guess reveals words only if present`() {
        // Given
        val policy = GuessingPolicy()
        val riddle = Riddle(Riddle.Id(1), listOf("Lamp", "Cloud", "Whisper").map(Word::of), ImageUrl("url"))

        // When
        val game0 = Game.start(riddle, policy)
        val (game1, _) = game0.applyGuess(listOf("lamp", "shouter").map(Word::of), policy)

        // Then
        assertEquals(listOf("lamp", "-----", "-------"), game1.board.shown)
    }

    @Test
    fun `give up reveals all words`() {
        // Given
        val policy = GuessingPolicy()
        val riddle = Riddle(Riddle.Id(1), listOf("Lamp", "Cloud", "Whisper").map(Word::of), ImageUrl("url"))

        // When
        val game0 = Game.start(riddle, policy)
        val (game1, _) = game0.giveUp(policy)

        // Then
        assertEquals(listOf("lamp", "cloud", "whisper"), game1.board.shown)
    }

    @Test
    fun `starting a game masks all words`() {
        // Given
        val policy = GuessingPolicy()
        val riddle = Riddle(Riddle.Id(1), listOf("Lamp", "Cloud", "Whisper").map(Word::of), ImageUrl("url"))

        // When
        val game = Game.start(riddle, policy)

        // Then
        assertEquals(listOf("----", "-----", "-------"), game.board.shown)
    }
}
