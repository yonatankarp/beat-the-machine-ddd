package com.yonatankarp.beatthemachine.domain.game

/**
 * Represents the current state of a game session.
 */
enum class GameState {
    ABANDONED,
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED,
}
