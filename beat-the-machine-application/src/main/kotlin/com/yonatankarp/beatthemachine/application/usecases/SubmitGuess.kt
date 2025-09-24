package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.input.SubmitGuessUseCase
import com.yonatankarp.beatthemachine.application.ports.output.FindGameById
import com.yonatankarp.beatthemachine.application.ports.output.SaveGame
import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.game.exceptions.GameNotFoundException
import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.GuessResult

/**
 * Submits a player's guess for evaluation against the current riddle.
 */
class SubmitGuess(
    private val findGameById: FindGameById,
    private val saveGame: SaveGame,
) : SubmitGuessUseCase {
    /**
     * Submits a player's guess for the specified game, persists the updated game state, and
     * returns the guess evaluation against the game's current riddle.
     *
     * @param gameId Identifier of the game to submit the guess to.
     * @param guess The player's guess to evaluate.
     * @return The result of evaluating the guess against the game's current riddle.
     * @throws GameNotFoundException if no game exists for the given [gameId].
     */
    override fun invoke(
        gameId: Game.Id,
        guess: Guess,
    ): GuessResult {
        val game = findGameById(gameId) ?: throw GameNotFoundException(gameId)
        val updatedGame = game.submitGuess(guess)
        saveGame(updatedGame)
        return game.currentRiddle.evaluate(guess)
    }
}
