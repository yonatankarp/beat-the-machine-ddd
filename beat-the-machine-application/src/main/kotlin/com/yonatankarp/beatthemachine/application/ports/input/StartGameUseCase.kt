package com.yonatankarp.beatthemachine.application.ports.input

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Use case for starting a new game.
 * Fetches available riddles and creates a new game session.
 */
fun interface StartGameUseCase {
    /**
 * Starts a new game session.
 *
 * Invoking this use case initializes a new Game by fetching available riddles and preparing initial game state.
 *
 * @return The newly created Game instance representing the started session.
 */
operator fun invoke(): Game
}
