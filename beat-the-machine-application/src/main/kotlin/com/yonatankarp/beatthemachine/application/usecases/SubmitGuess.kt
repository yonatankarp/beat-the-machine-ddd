package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.input.SubmitGuessUseCase
import com.yonatankarp.beatthemachine.application.ports.output.FindGameById
import com.yonatankarp.beatthemachine.application.ports.output.SaveGame
import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.game.exceptions.DomainError
import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.GuessResult

/**
 * Submits a player's guess for evaluation against the current riddle.
 */
class SubmitGuess(
    private val findGameById: FindGameById,
    private val saveGame: SaveGame,
) : SubmitGuessUseCase {
    override fun invoke(
        gameId: Game.Id,
        guess: Guess,
    ): GuessResult {
        val game = findGameById(gameId) ?: throw DomainError.GameNotFound(gameId)
        val updatedGame = game.submitGuess(guess)
        saveGame(updatedGame)
        return game.currentRiddle.evaluate(guess)
    }
}
