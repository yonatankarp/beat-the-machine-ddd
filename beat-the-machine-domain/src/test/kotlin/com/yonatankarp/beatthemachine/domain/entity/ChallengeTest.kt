package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ChallengeTest {
    private fun newChallenge(
        prompt: String = "hello world",
        lives: Int = 3,
    ) = Challenge.start(Prompt(prompt), Lives(lives))

    @Test
    fun `a correct guess reveals the word and stays in progress`() {
        // Given
        val challenge = newChallenge()
        val guess = Guess("hello")

        // When
        val (updated, outcome) = challenge.makeGuess(guess)

        // Then
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.IN_PROGRESS, updated.status)
        assertEquals(3, updated.lives.remaining)
    }

    @Test
    fun `a wrong guess costs a life`() {
        // Given
        val challenge = newChallenge()
        val guess = Guess("nope")

        // When
        val (updated, outcome) = challenge.makeGuess(guess)

        // Then
        assertEquals(GuessOutcome.MISS, outcome)
        assertEquals(2, updated.lives.remaining)
    }

    @Test
    fun `guessing every word beats the machine`() {
        // Given
        val challenge = newChallenge()
        val (afterFirst, _) = challenge.makeGuess(Guess("hello"))

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess(Guess("world"))

        // Then
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.BEATEN, afterSecond.status)
    }

    @Test
    fun `running out of lives loses the challenge`() {
        // Given
        val challenge = newChallenge(lives = 1)
        val guess = Guess("nope")

        // When
        val (updated, _) = challenge.makeGuess(guess)

        // Then
        assertEquals(ChallengeStatus.LOST, updated.status)
    }

    @Test
    fun `a duplicate guess is a no-op and costs no life`() {
        // Given
        val challenge = newChallenge()
        val (afterFirst, _) = challenge.makeGuess(Guess("nope"))

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess(Guess("Nope"))

        // Then
        assertEquals(GuessOutcome.DUPLICATE, outcome)
        assertEquals(2, afterSecond.lives.remaining)
    }

    @Test
    fun `makeGuess does not mutate the receiver`() {
        // Given
        val challenge = newChallenge()
        val guess = Guess("nope")

        // When
        challenge.makeGuess(guess)

        // Then
        assertEquals(3, challenge.lives.remaining)
        assertTrue(challenge.guesses.isEmpty())
    }

    @Test
    fun `guessing after the challenge is over is rejected`() {
        // Given
        val (lost, _) = newChallenge(lives = 1).makeGuess(Guess("nope"))

        // When / Then
        assertFailsWith<ChallengeAlreadyOver> { lost.makeGuess(Guess("hello")) }
    }

    @Test
    fun `forfeit reveals the prompt and loses`() {
        // Given
        val challenge = newChallenge()

        // When
        val forfeited = challenge.forfeit()

        // Then
        assertEquals(ChallengeStatus.LOST, forfeited.status)
        assertTrue(forfeited.maskedPrompt().tokens.all { it is MaskedToken.Revealed })
    }

    @Test
    fun `forfeit after the challenge is over is rejected`() {
        // Given
        val forfeited = newChallenge().forfeit()

        // When / Then
        assertFailsWith<ChallengeAlreadyOver> { forfeited.forfeit() }
    }

    @Test
    fun `withPicture returns an independent copy at the same version`() {
        // Given
        val challenge = newChallenge()
        val newPicture = Picture.Ready("https://example.com/img.png")

        // When
        val updated = challenge.withPicture(newPicture)

        // Then
        assertEquals(newPicture, updated.picture)
        assertEquals(challenge.version, updated.version)
        assertEquals(Picture.Pending, challenge.picture)
        assertNotSame(challenge, updated)
    }
}
