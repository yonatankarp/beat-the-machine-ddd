package com.yonatankarp.beatthemachine.application.ports.input

import com.yonatankarp.beatthemachine.domain.game.Game
import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.GuessResult

/**
 * Use case for submitting a guess to a game.
 * Evaluates the guess against the current riddle and returns feedback.
 */
fun interface SubmitGuessUseCase {
    operator fun invoke(
        gameId: Game.Id,
        guess: Guess,
    ): GuessResult
}
