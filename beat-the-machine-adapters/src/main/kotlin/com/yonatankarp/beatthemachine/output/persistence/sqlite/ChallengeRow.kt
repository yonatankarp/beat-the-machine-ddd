package com.yonatankarp.beatthemachine.output.persistence.sqlite

data class ChallengeRow(
    val id: String,
    val prompt: String,
    val guesses: String,
    val lives: Int,
    val status: String,
    val pictureStatus: String,
    val pictureUrl: String?,
    val difficulty: String,
    val version: Long,
)
