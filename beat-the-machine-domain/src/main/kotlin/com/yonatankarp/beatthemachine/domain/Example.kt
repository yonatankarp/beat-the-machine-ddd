package com.yonatankarp.beatthemachine.domain

/**
 * Temporary example class for build validation. This class provides basic
 * string operations and will be replaced with actual domain models.
 */
data class Example(
    val value: String,
) {
    fun isEmpty(): Boolean = value.isBlank()

    fun toUpperCase(): String = value.uppercase()
}
