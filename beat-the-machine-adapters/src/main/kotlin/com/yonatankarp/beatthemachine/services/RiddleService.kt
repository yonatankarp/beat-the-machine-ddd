package com.yonatankarp.beatthemachine.services

import com.yonatankarp.beatthemachine.models.GuessRequest
import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult
import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.HIT
import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.MISS
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RiddleService {
    companion object {
        private const val MASK_CHARACTER = "-"

        private val log = LoggerFactory.getLogger(RiddleService::class.java)
        private val WHITESPACE_REGEX = """\s+""".toRegex()
    }

    fun handleGuess(
        id: Int,
        guess: GuessRequest,
    ): List<Pair<String, GuessResult>> {
        val riddle = getRiddle(id)
        return guess.words?.let {
            val guesses =
                guess.words
                    ?.split(" ")
                    ?.map { it.lowercase() }
                    ?.toList() ?: emptyList()

            return maskNoneGuessedWords(guesses, riddle.prompt)
                .also { log.info("Phrase '${riddle.prompt}' with guess $guess have the results: $it") }
        } ?: riddle.initPrompt()
    }

    fun maskNoneGuessedWords(
        words: List<String>,
        prompt: String,
    ): List<Pair<String, GuessResult>> =
        prompt
            .lowercase()
            .split(WHITESPACE_REGEX)
            .map { word ->
                if (words.contains(word)) {
                    word to HIT
                } else {
                    MASK_CHARACTER.repeat(word.length) to MISS
                }
            }

    fun getRiddle(id: Int) = RiddleManager.riddles[id]

    fun getRandomRiddle() =
        getRiddle(Random.nextInt(from = 0, until = RiddleManager.numberOfRiddles))
            .also { log.info("Reading riddle #$it") }
}
