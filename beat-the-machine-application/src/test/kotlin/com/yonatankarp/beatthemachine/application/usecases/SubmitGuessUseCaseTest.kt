package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.output.FindGameById
import com.yonatankarp.beatthemachine.application.ports.output.SaveGame
import com.yonatankarp.beatthemachine.domain.fixtures.GameFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.GuessFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.RiddleFixtures
import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.riddle.WordFeedback
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SubmitGuessUseCaseTest {
    @Test
    fun `should submit guess and return evaluation result`() {
        // Given
        val game = GameFixtures.SingleRiddle.simpleGame()
        val guess = GuessFixtures.Perfect.helloWorld()
        val submitGuessUseCase = createSubmitGuessUseCase(game)

        // When
        val result = submitGuessUseCase(game.id, guess)

        // Then
        result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        result[1] shouldBe WordFeedback.Status.CORRECT_POSITION
    }

    @Test
    fun `should submit partial guess and return mixed feedback`() {
        // Given
        val game = Game.start(RiddleFixtures.Evaluation.complexEvaluation())
        val guess = GuessFixtures.Evaluation.mixedPositions()
        val submitGuessUseCase = createSubmitGuessUseCase(game)

        // When
        val result = submitGuessUseCase(game.id, guess)

        // Then
        result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        result[1] shouldBe WordFeedback.Status.WRONG_POSITION
        result[2] shouldBe WordFeedback.Status.CORRECT_POSITION
    }

    @Test
    fun `should submit wrong guess and return all wrong feedback`() {
        // Given
        val game = GameFixtures.SingleRiddle.photographyGame()
        val guess = GuessFixtures.Wrong.completelyWrong()
        val submitGuessUseCase = createSubmitGuessUseCase(game)

        // When
        val result = submitGuessUseCase(game.id, guess)

        // Then
        result.feedbacks.all { it.status == WordFeedback.Status.WRONG_WORD } shouldBe true
    }

    @Test
    fun `should handle complex evaluation scenario`() {
        // Given
        val game = Game.start(RiddleFixtures.Evaluation.longPrompt())
        val guess = GuessFixtures.EdgeCases.tooLong()
        val submitGuessUseCase = createSubmitGuessUseCase(game)

        // When
        val result = submitGuessUseCase(game.id, guess)

        // Then
        result.feedbacks.size shouldBe guess.words.size
    }

    private fun createSubmitGuessUseCase(game: Game): SubmitGuess {
        val findGameById = FindGameById { game }
        val saveGame = SaveGame { it }
        return SubmitGuess(findGameById, saveGame)
    }
}
