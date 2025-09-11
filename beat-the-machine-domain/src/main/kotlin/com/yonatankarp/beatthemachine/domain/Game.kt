package com.yonatankarp.beatthemachine.domain

import com.yonatankarp.beatthemachine.domain.events.GameEvent.GameGivenUp
import com.yonatankarp.beatthemachine.domain.events.GameEvent.GuessEvaluated

data class Game(
    val riddle: Riddle,
    val board: Board,
) {
    fun applyGuess(
        guess: List<Word>,
        policy: GuessingPolicy,
    ): Pair<Game, GuessEvaluated> {
        val nextShown = policy.reveal(riddle.prompt, board.shown, guess)
        val revealed = nextShown.count { it.first != it.second }
        val next = copy(board = Board(nextShown.map { it.second }))
        return next to GuessEvaluated(riddle.id, revealed)
    }

    fun giveUp(policy: GuessingPolicy): Pair<Game, GameGivenUp> {
        val revealed = policy.revealAll(riddle.prompt)
        return copy(board = Board(revealed)) to GameGivenUp(riddle.id)
    }

    companion object {
        fun start(
            riddle: Riddle,
            policy: GuessingPolicy,
        ): Game =
            Game(
                riddle = riddle,
                board = Board(policy.initialMask(riddle.prompt)),
            )
    }
}
