package com.yonatankarp.beatthemachine.domain

@JvmInline
value class Word private constructor(val value: String) {

    fun masked() = "-".repeat(value.length)

    companion object {
        fun of(s: String): Word {
            require(s.trim().split("\\s+".toRegex()).size == 1) { "Word must be a single word" }
            return Word(s.lowercase().trim())
        }
    }
}
