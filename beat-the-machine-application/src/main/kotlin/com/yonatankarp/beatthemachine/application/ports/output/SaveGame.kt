package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Output port for saving a game.
 */
fun interface SaveGame {
    operator fun invoke(game: Game): Game
}
