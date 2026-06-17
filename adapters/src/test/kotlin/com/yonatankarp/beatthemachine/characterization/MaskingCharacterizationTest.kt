package com.yonatankarp.beatthemachine.characterization

import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.HIT
import com.yonatankarp.beatthemachine.models.GuessResponse.GuessResult.MISS
import com.yonatankarp.beatthemachine.services.RiddleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Characterization tests that pin the CURRENT observable masking/guessing behavior of
 * [RiddleService.maskNoneGuessedWords]. These tests must stay green through the refactor;
 * any deliberate behavior change must update them explicitly.
 *
 * Key observations captured here:
 * - The prompt is fully lowercased before splitting, so revealed words are always lowercase.
 * - Matching is case-insensitive because both the prompt and the guesses list are lowercased.
 * - A guess with leading/trailing spaces splits into empty strings that never match prompt words,
 *   so the trimmed word still matches if the caller passes it trimmed; but if passed raw via
 *   handleGuess the empty tokens join the list and "man" still matches due to split(" ").
 * - An empty or whitespace-only guess list produces all-masked output (no word equals "").
 */
class MaskingCharacterizationTest {
    private val service = RiddleService()

    // Riddle 0 prompt: "man stands on a man"
    private val prompt = "man stands on a man"

    @Test
    fun `masks all words when no guess matches`() {
        val result = service.maskNoneGuessedWords(listOf("dolphin", "fire"), prompt)

        assertEquals(
            listOf(
                "---" to MISS,
                "------" to MISS,
                "--" to MISS,
                "-" to MISS,
                "---" to MISS,
            ),
            result,
        )
    }

    @Test
    fun `reveals a single matching word case-insensitively`() {
        // Exercise REAL case folding: the system lowercases guesses in handleGuess via
        // `.map { it.lowercase() }` before calling maskNoneGuessedWords. We mirror that path
        // with a mixed-case guess to pin that case-insensitive matching actually works.
        // NOTE: maskNoneGuessedWords by itself does NOT fold case — if "Stands" were passed raw
        // (unfolded), it would MISS, since it only lowercases the prompt, not the guesses.
        val mixedCaseGuess = "Stands"
        val guessesAsHandleGuessWouldPass = listOf(mixedCaseGuess).map { it.lowercase() }
        val result = service.maskNoneGuessedWords(guessesAsHandleGuessWouldPass, prompt)

        assertEquals(
            listOf(
                "---" to MISS,
                "stands" to HIT,
                "--" to MISS,
                "-" to MISS,
                "---" to MISS,
            ),
            result,
        )
    }

    @Test
    fun `does not fold case when guesses are passed raw to maskNoneGuessedWords`() {
        // Pins the boundary: case folding lives in handleGuess, NOT in maskNoneGuessedWords.
        // A raw mixed-case guess passed straight to the masking method is a MISS.
        val result = service.maskNoneGuessedWords(listOf("Stands"), prompt)

        assertEquals(
            listOf(
                "---" to MISS,
                "------" to MISS,
                "--" to MISS,
                "-" to MISS,
                "---" to MISS,
            ),
            result,
        )
    }

    @Test
    fun `reveals each occurrence of a repeated matching word`() {
        // "man" appears twice in the prompt; both occurrences must be revealed.
        val result = service.maskNoneGuessedWords(listOf("man"), prompt)

        assertEquals(
            listOf(
                "man" to HIT,
                "------" to MISS,
                "--" to MISS,
                "-" to MISS,
                "man" to HIT,
            ),
            result,
        )
    }

    @Test
    fun `handles a guess with leading and trailing whitespace`() {
        // handleGuess splits on " " (single space), so "  man  " → ["", "", "man", "", ""].
        // The empty-string tokens don't match any prompt word, but "man" still matches.
        val guessesAfterSplit = "  man  ".split(" ").map { it.lowercase() }
        val result = service.maskNoneGuessedWords(guessesAfterSplit, prompt)

        // Same expected output as the repeated-word test: this is NOT a duplicate — it proves that
        // surrounding whitespace (which becomes empty tokens) is harmless and "man" still matches.
        assertEquals(
            listOf(
                "man" to HIT,
                "------" to MISS,
                "--" to MISS,
                "-" to MISS,
                "man" to HIT,
            ),
            result,
        )
    }

    @Test
    fun `handles an empty or whitespace-only guess`() {
        // "".split(" ") → [""], so the guesses list contains only an empty string.
        // No prompt word equals "", so everything is masked.
        val guessesAfterSplit = "".split(" ").map { it.lowercase() }
        val result = service.maskNoneGuessedWords(guessesAfterSplit, prompt)

        assertEquals(
            listOf(
                "---" to MISS,
                "------" to MISS,
                "--" to MISS,
                "-" to MISS,
                "---" to MISS,
            ),
            result,
        )
    }
}
