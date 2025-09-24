package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.Word

/**
 * Represents a player's guess as a collection of words.
 *
 * Provides factory methods for convenient construction from varargs or space-separated text.
 *
 * @property words The list of guessed words
 * @throws IllegalArgumentException if the guess is empty
 */
data class Guess(
    val words: List<Word>,
) {
    init {
        require(words.isNotEmpty()) { "Guess cannot be empty" }
    }

    companion object {
        fun of(vararg strings: String) = Guess(strings.map { Word(it) })

        fun from(text: String) = Guess(text.split(" ").map { Word(it) })
    }
}
