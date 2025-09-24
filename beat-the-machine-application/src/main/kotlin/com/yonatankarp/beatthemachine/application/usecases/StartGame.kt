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
    /**
     * Starts a new Game using the currently available riddles.
     *
     * Retrieves riddles via the injected FindAvailableRiddles port and returns a started Game instance.
     *
     * @return a new Game initialized with the available riddles.
     */
    override fun invoke(): Game {
        val riddles = findAvailableRiddles()
        return Game.start(riddles)
    }
}
