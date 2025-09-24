package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.riddle.Guess

/**
 * Represents the history of all guess attempts made for a riddle.
 */
data class AttemptHistory(
    private val guesses: List<Guess> = emptyList(),
) {
    val isEmpty: Boolean
        get() = guesses.isEmpty()

    val count: AttemptNumber
        get() = AttemptNumber(guesses.size)

    fun addGuess(guess: Guess) = AttemptHistory(guesses + guess)

    companion object {
        fun empty() = AttemptHistory()
    }
}
