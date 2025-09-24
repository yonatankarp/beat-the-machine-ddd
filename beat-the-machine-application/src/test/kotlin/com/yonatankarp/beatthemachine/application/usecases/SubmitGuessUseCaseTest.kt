package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.output.FindGameById
import com.yonatankarp.beatthemachine.application.ports.output.SaveGame
import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.ImageUrl
import com.yonatankarp.beatthemachine.domain.riddle.Prompt
import com.yonatankarp.beatthemachine.domain.riddle.Riddle
import com.yonatankarp.beatthemachine.domain.riddle.WordFeedback
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SubmitGuessUseCaseTest {
    @Test
    fun `should submit guess and return evaluation result`() {
        // Given
        val riddle = Riddle(Prompt.of("hello", "world"), ImageUrl("https://example.com/image.jpg"))
        val game = Game.start(listOf(riddle))
        val guess = Guess.of("hello", "world")

        val findGameById = FindGameById { game }
        val saveGame = SaveGame { it }

        val submitGuess = SubmitGuess(findGameById, saveGame)

        // When
        val result = submitGuess(game.id, guess)

        // Then
        result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        result[1] shouldBe WordFeedback.Status.CORRECT_POSITION
    }
}
