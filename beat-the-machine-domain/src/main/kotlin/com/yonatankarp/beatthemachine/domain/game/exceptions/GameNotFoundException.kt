package com.yonatankarp.beatthemachine.domain.game.exceptions

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Exception thrown when a game is not found.
 *
 * @property gameId The ID of the game that was not found
 */
class GameNotFoundException(
    gameId: Game.Id,
) : RuntimeException("Game not found: ${gameId.value}")
