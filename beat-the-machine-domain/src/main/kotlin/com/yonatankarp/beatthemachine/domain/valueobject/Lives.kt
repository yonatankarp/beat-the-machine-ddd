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
}
