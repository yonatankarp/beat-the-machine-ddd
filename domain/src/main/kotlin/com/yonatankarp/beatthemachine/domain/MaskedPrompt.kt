package com.yonatankarp.beatthemachine.domain

sealed interface MaskedToken {
    data class Revealed(
        val word: String,
    ) : MaskedToken

    data object Hidden : MaskedToken
}

class MaskedPrompt private constructor(
    val tokens: List<MaskedToken>,
) {
    fun isFullyRevealed(): Boolean = tokens.all { it is MaskedToken.Revealed }

    companion object {
        fun of(
            prompt: Prompt,
            guesses: Set<Guess>,
        ): MaskedPrompt {
            val guessed = guesses.map { it.normalized() }.toSet()
            return MaskedPrompt(
                prompt.words().map { word ->
                    if (word.lowercase() in guessed) {
                        MaskedToken.Revealed(word)
                    } else {
                        MaskedToken.Hidden
                    }
                },
            )
        }
    }
}
