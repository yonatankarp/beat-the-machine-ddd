package com.yonatankarp.beatthemachine.domain.game

/**
 * Represents the maximum number of attempts allowed in a game.
 */
@JvmInline
value class MaxAttempts(
    val value: Int,
) {
    init {
        require(value > 0) { "Max attempts must be positive" }
    }

    fun isExceeded(attemptNumber: AttemptNumber) = attemptNumber.value > value
}
