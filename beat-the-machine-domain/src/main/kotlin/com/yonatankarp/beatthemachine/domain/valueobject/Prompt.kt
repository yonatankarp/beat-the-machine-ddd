package com.yonatankarp.beatthemachine.domain.valueobject

@JvmInline
value class Prompt(
    val text: String,
) {
    init {
        require(text.isNotBlank()) { "prompt must not be blank" }
    }

    fun words(): List<String> = text.trim().split(WHITESPACE)

    companion object {
        private val WHITESPACE = Regex("\\s+")
    }
}
