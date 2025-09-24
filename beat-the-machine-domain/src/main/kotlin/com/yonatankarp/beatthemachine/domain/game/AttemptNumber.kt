package com.yonatankarp.beatthemachine.domain.game

/**
 * Represents the current attempt number in a game.
 */
@JvmInline
value class AttemptNumber(
    val value: Int,
) {
    init {
        require(value >= 0) { "Attempt number must be non-negative" }
    }

    fun increment() = AttemptNumber(value + 1)
}
