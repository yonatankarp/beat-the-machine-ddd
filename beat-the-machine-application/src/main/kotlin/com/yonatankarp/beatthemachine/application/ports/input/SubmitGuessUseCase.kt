package com.yonatankarp.beatthemachine.application.ports.input

import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.GuessResult

/**
 * Use case for submitting a guess to a game.
 * Evaluates the guess against the current riddle and returns feedback.
 */
fun interface SubmitGuessUseCase {
    /**
     * Submit a guess for the specified game and evaluate it against the game's current riddle.
     *
     * Evaluates the provided `guess` in the context of the game identified by `gameId` and returns
     * a `GuessResult` describing the outcome (e.g., correctness and any hint or feedback).
     *
     * @param gameId Identifier of the game that the guess applies to.
     * @param guess The player's guess to be evaluated.
     * @return Feedback about the submitted guess as a `GuessResult`.
     */
    operator fun invoke(
        gameId: Game.Id,
        guess: Guess,
    ): GuessResult
}
