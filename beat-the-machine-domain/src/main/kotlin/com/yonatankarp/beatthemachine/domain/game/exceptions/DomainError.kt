package com.yonatankarp.beatthemachine.domain.game.exceptions

import com.yonatankarp.beatthemachine.domain.game.Game

sealed interface DomainError {
    class GameNotFound(
        gameId: Game.Id,
    ) : RuntimeException("Game not found: ${gameId.value}"), DomainError
}