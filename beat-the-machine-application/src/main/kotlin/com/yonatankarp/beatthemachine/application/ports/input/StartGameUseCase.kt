package com.yonatankarp.beatthemachine.application.ports.input

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Use case for starting a new game.
 * Fetches available riddles and creates a new game session.
 */
fun interface StartGameUseCase {
    operator fun invoke(): Game
}
