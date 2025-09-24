package com.yonatankarp.beatthemachine.domain.game

import java.util.UUID

/**
 * UUID-based unique identifier for game sessions.
 *
 * @property value The UUID representing the game session
 */
data class GameId(
    val value: UUID,
)
