package com.yonatankarp.beatthemachine.output.ai

fun interface LlmText {
    suspend fun complete(
        system: String,
        user: String,
    ): String
}
