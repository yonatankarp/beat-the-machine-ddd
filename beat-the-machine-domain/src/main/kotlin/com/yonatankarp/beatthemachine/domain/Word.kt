package com.yonatankarp.beatthemachine.domain

/**
 * Represents a single word in the domain model.
 *
 * A Word is a value object that ensures single-word constraint and provides case-insensitive comparison.
 * Words are normalized to lowercase and can be obfuscated for display purposes.
 *
 * @property input The raw word input (will be normalized to lowercase)
 * @throws IllegalArgumentException if the input contains spaces (multi-word text)
 */
data class Word(
    private val input: String,
) {
    val value: String = input.lowercase()

    init {
        require(input.contains(' ').not()) { "Text must be a single word" }
    }

    fun obfuscated() = "-".repeat(value.length)
}
