package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.Word

/**
 * Represents an ordered list of words that form the AI prompt used to generate an image.
 *
 * Provides factory methods for convenient construction from varargs or space-separated text.
 * Supports infix contains operator for natural domain language.
 *
 * @property words The ordered list of words in the prompt
 * @throws IllegalArgumentException if the prompt is empty
 */
data class Prompt(
    val words: List<Word>,
) {
    init {
        require(words.isNotEmpty()) { "Prompt cannot be empty" }
    }

    infix fun contains(word: Word) = words.contains(word)

    companion object {
        fun of(vararg strings: String) = Prompt(strings.map { Word(it) })

        fun from(text: String) = Prompt(text.split(" ").map { Word(it) })
    }
}
