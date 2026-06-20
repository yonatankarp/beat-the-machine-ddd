package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.slf4j.LoggerFactory

class SpringAiPromptSource(
    private val llm: LlmText,
    private val fallback: PromptSource,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
) : PromptSource {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend infix fun next(difficulty: Difficulty): Prompt {
        val band = bandFor(difficulty)
        repeat(maxAttempts) { attempt ->
            val candidate =
                runCatching { llm.complete(SYSTEM_PROMPT, userPrompt(difficulty, band)) }
                    .getOrElse { llmError ->
                        logger.warn("LLM call failed (attempt {}/{})", attempt + 1, maxAttempts, llmError)
                        return@repeat
                    }
            val phrase = candidate.trim()
            if (phrase.isNotBlank() && phrase.split(WHITESPACE_REGEX).size in band) {
                return Prompt(phrase)
            }
            logger.info("Discarding out-of-band LLM phrase: '{}'", phrase)
        }
        logger.warn("LLM produced no valid phrase in {} attempts; using fallback", maxAttempts)
        return fallback next difficulty
    }

    private fun userPrompt(
        difficulty: Difficulty,
        band: IntRange,
    ): String =
        "Give exactly one concrete, visually depictable ${difficulty.name.lowercase()} subject " +
            "for an AI image, between ${band.first} and ${band.last} words. " +
            "Reply with only the phrase, no punctuation, no quotes, no explanation."

    private fun bandFor(difficulty: Difficulty): IntRange =
        when (difficulty) {
            Difficulty.EASY -> EASY_BAND
            Difficulty.MEDIUM -> MEDIUM_BAND
            Difficulty.HARD -> HARD_BAND
        }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        val EASY_BAND = 1..2
        val MEDIUM_BAND = 2..3
        val HARD_BAND = 3..5
        val WHITESPACE_REGEX = Regex("\\s+")
        const val SYSTEM_PROMPT =
            "You generate short, concrete, family-friendly subjects for an AI image-guessing game. " +
                "Favour vivid, drawable nouns and scenes. Never use proper names or offensive content."
    }
}
