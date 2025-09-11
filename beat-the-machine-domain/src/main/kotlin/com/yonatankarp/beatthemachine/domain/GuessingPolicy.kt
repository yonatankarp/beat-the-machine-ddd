package com.yonatankarp.beatthemachine.domain

class GuessingPolicy {
    fun initialMask(target: List<Word>): List<String> = target.map { it.masked() }

    fun reveal(
        target: List<Word>,
        current: List<String>,
        guess: List<Word>,
    ): List<Pair<String, String>> {
        val g = guess.map { it.value }.toSet()
        return target.zip(current).map { (targetWord, currentShown) ->
            val next =
                if (g.contains(targetWord.value)) {
                    targetWord.value
                } else {
                    currentShown
                }
            currentShown to next
        }
    }

    fun revealAll(target: List<Word>): List<String> = target.map { it.value }
}
