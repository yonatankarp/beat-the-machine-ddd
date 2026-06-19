package com.yonatankarp.beatthemachine.domain.valueobject

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
        fun initialFor(difficulty: Difficulty): Lives =
            when (difficulty) {
                Difficulty.EASY -> Lives(8)
                Difficulty.MEDIUM -> Lives(6)
                Difficulty.HARD -> Lives(4)
            }
    }
}
