package com.yonatankarp.beatthemachine.application.ports.output

import com.yonatankarp.beatthemachine.domain.game.Game

/**
 * Output port for saving a game.
 */
fun interface SaveGame {
    /**
 * Saves the provided Game and returns the saved instance.
 *
 * Implementations persist the given game and return the resulting Game,
 * which may include updates applied by the persistence layer (for example an assigned id or timestamps).
 *
 * @param game the game to save
 * @return the saved Game instance
 */
operator fun invoke(game: Game): Game
}
