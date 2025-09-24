package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.riddle.Guess

/**
 * Represents the history of all guess attempts made for a riddle.
 */
data class AttemptHistory(
    private val guesses: List<Guess> = emptyList(),
) {
    fun isEmpty() = guesses.isEmpty()

    fun addGuess(guess: Guess) = AttemptHistory(guesses + guess)

    fun count() = AttemptNumber(guesses.size)
}
