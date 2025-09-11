package com.yonatankarp.beatthemachine.domain

data class Board(
    val shown: List<String>,
) {
    fun isSolved(target: List<Word>): Boolean = shown.map { it.lowercase() } == target.map { it.value }
}
