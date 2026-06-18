package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedPrompt
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

/**
 * The Challenge aggregate root. Immutable: every state transition returns a new
 * instance and leaves the receiver untouched. The version is never changed by a
 * domain operation; the persistence adapter is the single writer that increments
 * it on save.
 */
class Challenge private constructor(
    val id: ChallengeId,
    private val prompt: Prompt,
    val guesses: Set<Guess>,
    val lives: Lives,
    val status: ChallengeStatus,
    val picture: Picture,
    val difficulty: Difficulty,
    val version: Long,
) {
    fun maskedPrompt(): MaskedPrompt =
        if (status == ChallengeStatus.LOST) {
            MaskedPrompt.of(prompt, prompt.allWordsAsGuesses())
        } else {
            MaskedPrompt.of(prompt, guesses)
        }

    fun makeGuess(guess: Guess): Pair<Challenge, GuessOutcome> {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        val normalized = guess.normalized()
        if (guesses.any { it.normalized() == normalized }) return this to GuessOutcome.DUPLICATE
        val updatedGuesses = guesses + guess
        val hit = prompt.words().any { it.lowercase() == normalized }
        return if (hit) {
            val beaten = MaskedPrompt.of(prompt, updatedGuesses).isFullyRevealed()
            val newStatus = if (beaten) ChallengeStatus.BEATEN else status
            copy(guesses = updatedGuesses, status = newStatus) to GuessOutcome.HIT
        } else {
            val remaining = lives.lose()
            val newStatus = if (remaining.isExhausted()) ChallengeStatus.LOST else status
            copy(guesses = updatedGuesses, lives = remaining, status = newStatus) to GuessOutcome.MISS
        }
    }

    fun forfeit(): Challenge {
        if (status != ChallengeStatus.IN_PROGRESS) throw ChallengeAlreadyOver(id)
        return copy(status = ChallengeStatus.LOST)
    }

    /**
     * Returns a copy carrying the new picture. The version is left untouched (the
     * persistence adapter increments it on save), so the async picture pipeline can
     * apply this to the latest persisted aggregate without forcing a version bump.
     */
    fun withPicture(picture: Picture): Challenge = copy(picture = picture)

    // Exposed only to persistence mapping in the adapter module.
    fun secretPrompt(): Prompt = prompt

    private fun copy(
        guesses: Set<Guess> = this.guesses,
        lives: Lives = this.lives,
        status: ChallengeStatus = this.status,
        picture: Picture = this.picture,
        version: Long = this.version,
    ): Challenge = Challenge(id, prompt, guesses, lives, status, picture, difficulty, version)

    companion object {
        fun start(
            prompt: Prompt,
            lives: Lives,
            picture: Picture = Picture.Pending,
            difficulty: Difficulty = Difficulty.MEDIUM,
        ): Challenge =
            Challenge(
                ChallengeId.new(),
                prompt,
                emptySet(),
                lives,
                ChallengeStatus.IN_PROGRESS,
                picture,
                difficulty,
                version = 0,
            )

        fun rehydrate(
            id: ChallengeId,
            prompt: Prompt,
            guesses: Set<Guess>,
            lives: Lives,
            status: ChallengeStatus,
            picture: Picture,
            difficulty: Difficulty,
            version: Long,
        ): Challenge = Challenge(id, prompt, guesses.toSet(), lives, status, picture, difficulty, version)
    }
}

private fun Prompt.allWordsAsGuesses(): Set<Guess> = words().map { Guess(it) }.toSet()
