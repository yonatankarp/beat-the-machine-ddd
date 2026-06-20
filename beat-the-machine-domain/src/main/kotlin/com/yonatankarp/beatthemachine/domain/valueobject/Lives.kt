package com.yonatankarp.beatthemachine.domain.valueobject

import kotlin.math.roundToInt

@JvmInline
value class Lives(
    val remaining: Int,
) {
    init {
        require(remaining >= 0) { "lives cannot be negative" }
    }

    fun lose(): Lives = Lives(maxOf(0, remaining - 1))

    fun isExhausted(): Boolean = remaining == 0

    companion object {
        private const val PER_WORD_BASE = 3
        private const val MIN_LIVES = 2

        fun initialFor(difficulty: Difficulty): Lives =
            when (difficulty) {
                Difficulty.EASY -> Lives(8)
                Difficulty.MEDIUM -> Lives(6)
                Difficulty.HARD -> Lives(4)
            }

        fun forSecret(
            prompt: Prompt,
            difficulty: Difficulty,
        ): Lives {
            val raw = (PER_WORD_BASE * prompt.words().size * difficulty.livesMultiplier).roundToInt()
            return Lives(maxOf(MIN_LIVES, raw))
        }
    }
}
