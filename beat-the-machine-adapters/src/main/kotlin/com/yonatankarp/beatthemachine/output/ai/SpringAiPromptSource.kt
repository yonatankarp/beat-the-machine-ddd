package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.slf4j.LoggerFactory

class SpringAiPromptSource(
    private val llm: LlmText,
    private val fallback: PromptSource,
    private val maxAttempts: Int = 3,
) : PromptSource {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend infix fun next(difficulty: Difficulty): Prompt {
        val band = bandFor(difficulty)
        repeat(maxAttempts) { attempt ->
            val candidate =
                runCatching { llm.complete(SYSTEM, userPrompt(difficulty, band)) }
                    .getOrElse { ex ->
                        logger.warn("LLM call failed (attempt {}/{})", attempt + 1, maxAttempts, ex)
                        return@repeat
                    }
            val phrase = candidate.trim()
            if (phrase.isNotBlank() && phrase.split(WHITESPACE).size in band) {
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
            Difficulty.EASY -> 1..2
            Difficulty.MEDIUM -> 2..3
            Difficulty.HARD -> 3..5
        }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val SYSTEM =
            "You generate short, concrete, family-friendly subjects for an AI image-guessing game. " +
                "Favour vivid, drawable nouns and scenes. Never use proper names or offensive content."
    }
}
