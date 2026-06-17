package com.yonatankarp.beatthemachine.models

import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.MISS

data class Riddle(
    val id: Int,
    val startPrompt: String,
    val prompt: String,
    val url: String,
) {
    fun giveUp() =
        this.prompt
            .split(" ")
            .map { it to MISS }

    fun initPrompt() =
        this.startPrompt
            .split(" ")
            .map { it to MISS }
            .toList()
}
