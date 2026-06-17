package com.yonatankarp.beatthemachine.domain

@JvmInline
value class Guess(
    val word: String,
) {
    init {
        require(word.isNotBlank()) { "guess must not be blank" }
    }

    fun normalized(): String = word.trim().lowercase()
}
