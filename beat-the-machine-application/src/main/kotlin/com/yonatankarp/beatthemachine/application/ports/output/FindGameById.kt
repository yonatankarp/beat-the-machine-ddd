package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Output port for finding a game by its ID.
 */
fun interface FindGameById {
    operator fun invoke(id: Game.Id): Game?
}
