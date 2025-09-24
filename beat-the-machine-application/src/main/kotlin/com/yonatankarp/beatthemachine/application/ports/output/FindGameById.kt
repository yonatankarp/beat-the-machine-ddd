package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Output port for finding a game by its ID.
 */
fun interface FindGameById {
    /**
 * Retrieve a Game by its identifier.
 *
 * Returns the Game with the given id, or `null` if no matching game exists.
 *
 * @param id The identifier of the game to find.
 * @return The found [Game], or `null` when not found.
 */
operator fun invoke(id: Game.Id): Game?
}
