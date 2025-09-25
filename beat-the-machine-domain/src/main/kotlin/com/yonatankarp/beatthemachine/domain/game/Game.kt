package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.Riddle
import java.util.UUID

/**
 * Game aggregate root managing the game session.
 */
data class Game(
    val id: Id,
    val riddles: List<Riddle>,
    val currentRiddleIndex: Int = 0,
    val state: State = State.IN_PROGRESS,
    val attemptHistory: AttemptHistory = AttemptHistory.empty(),
) {
    val isComplete: Boolean
        get() = currentRiddleIndex >= riddles.size

    val currentRiddle: Riddle
        get() = riddles[currentRiddleIndex]

    fun submitGuess(guess: Guess) = copy(attemptHistory = attemptHistory.addGuess(guess))

    fun nextRiddle() = copy(currentRiddleIndex = currentRiddleIndex + 1)

    /**
     * UUID-based unique identifier for game sessions.
     *
     * @property value The UUID representing the game session
     */
    data class Id(
        val value: UUID,
    ) {
        companion object {
            fun new() = Id(value = UUID.randomUUID())
        }
    }

    /**
     * Represents the current state of a game session.
     */
    enum class State {
        ABANDONED,
        COMPLETED,
        IN_PROGRESS,
    }

    companion object {
        fun start(riddles: List<Riddle> = emptyList()) = Game(id = Id.new(), riddles = riddles)

        fun start(vararg riddles: Riddle) = start(riddles = riddles.toList())
    }
}
