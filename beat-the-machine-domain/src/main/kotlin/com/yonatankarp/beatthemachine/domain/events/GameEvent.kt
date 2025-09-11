package com.yonatankarp.beatthemachine.domain.events

import com.yonatankarp.beatthemachine.domain.Riddle

sealed interface GameEvent {
    data class GuessEvaluated(
        val riddleId: Riddle.Id,
        val revealed: Int,
    ) : GameEvent

    data class GameGivenUp(
        val riddleId: Riddle.Id,
    ) : GameEvent
}
