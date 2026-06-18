package com.yonatankarp.beatthemachine.domain.valueobject

import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess

@JvmInline
value class Guess(
    val word: String,
) {
    init {
        if (word.isBlank()) throw InvalidGuess("guess must not be blank")
    }

    fun normalized(): String = word.trim().lowercase()
}
