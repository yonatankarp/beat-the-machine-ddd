package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ChallengeTest {
    @Test
    fun `a correct guess reveals the word and stays in progress`() {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "hello".asGuess()

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
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "nope".asGuess()

        // When
        val (updated, outcome) = challenge.makeGuess(guess)

        // Then
        assertEquals(GuessOutcome.MISS, outcome)
        assertEquals(2, updated.lives.remaining)
    }

    @Test
    fun `guessing every word beats the machine`() {
        // Given
        val challenge = mediumChallenge()
        val (afterFirst, _) = challenge.makeGuess("hello".asGuess())

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess("world".asGuess())

        // Then
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.BEATEN, afterSecond.status)
    }

    @Test
    fun `running out of lives loses the challenge`() {
        // Given
        val challenge = mediumChallenge(lives = 1.lives())
        val guess = "nope".asGuess()

        // When
        val (updated, _) = challenge.makeGuess(guess)

        // Then
        assertEquals(ChallengeStatus.LOST, updated.status)
    }

    @Test
    fun `a duplicate guess is a no-op and costs no life`() {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val (afterFirst, _) = challenge.makeGuess("nope".asGuess())

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess("Nope".asGuess())

        // Then
        assertEquals(GuessOutcome.DUPLICATE, outcome)
        assertEquals(2, afterSecond.lives.remaining)
    }

    @Test
    fun `makeGuess does not mutate the receiver`() {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "nope".asGuess()

        // When
        challenge.makeGuess(guess)

        // Then
        assertEquals(3, challenge.lives.remaining)
        assertTrue(challenge.guesses.isEmpty())
    }

    @Test
    fun `guessing after the challenge is over is rejected`() {
        // Given
        val (lost, _) = mediumChallenge(lives = 1.lives()).makeGuess("nope".asGuess())

        // When / Then
        assertFailsWith<ChallengeAlreadyOver> { lost.makeGuess("hello".asGuess()) }
    }

    @Test
    fun `forfeit reveals the prompt and loses`() {
        // Given
        val challenge = mediumChallenge()

        // When
        val forfeited = challenge.forfeit()

        // Then
        assertEquals(ChallengeStatus.LOST, forfeited.status)
        assertTrue(forfeited.maskedPrompt().tokens.all { it is MaskedToken.Revealed })
    }

    @Test
    fun `forfeit after the challenge is over is rejected`() {
        // Given
        val forfeited = mediumChallenge().forfeit()

        // When / Then
        assertFailsWith<ChallengeAlreadyOver> { forfeited.forfeit() }
    }

    @Test
    fun `withPicture returns an independent copy at the same version`() {
        // Given
        val challenge = mediumChallenge()
        val newPicture = readyPicture("https://example.com/img.png")

        // When
        val updated = challenge.withPicture(newPicture)

        // Then
        assertEquals(newPicture, updated.picture)
        assertEquals(challenge.version, updated.version)
        assertEquals(Picture.Pending, challenge.picture)
        assertNotSame(challenge, updated)
    }

    @Test
    fun `maxLives derives from the challenge secret and difficulty`() {
        // Given
        val challenge = mediumChallenge()

        // When
        val max = challenge.maxLives()

        // Then
        assertEquals(Lives(6), max) // round(3 * 2 * 1.0) = 6
    }

    @Test
    fun `maskedPrompt hides unguessed words while still in progress`() {
        // Given
        val (afterHit, _) = mediumChallenge().makeGuess("hello".asGuess())

        // When
        val masked = afterHit.maskedPrompt()

        // Then
        assertEquals(MaskedToken.Revealed("hello"), masked.tokens[0])
        assertEquals(MaskedToken.Hidden(5), masked.tokens[1])
    }

    @Test
    fun `start defaults to a pending picture and medium difficulty`() {
        // Given
        val prompt = "hello world".asPrompt()
        val lives = 6.lives()

        // When
        val challenge = Challenge.start(prompt, lives)

        // Then
        assertEquals(Picture.Pending, challenge.picture)
        assertEquals(Difficulty.MEDIUM, challenge.difficulty)
        assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
    }

    @Test
    fun `rehydrate reconstitutes a challenge and preserves the secret prompt`() {
        // Given
        val prompt = "secret phrase".asPrompt()

        // When
        val challenge =
            Challenge.rehydrate(
                id = aChallengeId(),
                prompt = prompt,
                guesses = emptySet(),
                lives = 4.lives(),
                status = ChallengeStatus.IN_PROGRESS,
                picture = Picture.Failed,
                difficulty = Difficulty.HARD,
                version = 7L,
            )

        // Then
        assertEquals(prompt, challenge.secretPrompt())
        assertEquals(4, challenge.lives.remaining)
        assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
        assertEquals(Picture.Failed, challenge.picture)
        assertEquals(Difficulty.HARD, challenge.difficulty)
        assertEquals(7L, challenge.version)
    }
}
