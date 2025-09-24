package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.input.StartGameUseCase
import com.yonatankarp.beatthemachine.application.ports.output.FindAvailableRiddles
import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Starts a new game with available riddles.
 */
class StartGame(
    private val findAvailableRiddles: FindAvailableRiddles,
) : StartGameUseCase {
    override fun invoke(): Game {
        val riddles = findAvailableRiddles()
        return Game.start(riddles)
    }
}
