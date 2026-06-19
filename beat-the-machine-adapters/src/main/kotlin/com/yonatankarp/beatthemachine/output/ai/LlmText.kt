package com.yonatankarp.beatthemachine.output.ai

/** Minimal text-completion seam over an LLM, isolating Spring AI's fluent API. */
fun interface LlmText {
    suspend fun complete(
        system: String,
        user: String,
    ): String
}
