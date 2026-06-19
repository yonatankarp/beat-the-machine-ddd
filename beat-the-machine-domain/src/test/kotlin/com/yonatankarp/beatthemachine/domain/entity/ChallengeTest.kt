package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
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
        val (updated, outcome) = newChallenge().makeGuess(Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.IN_PROGRESS, updated.status)
        assertEquals(3, updated.lives.remaining)
    }

    @Test
    fun `a wrong guess costs a life`() {
        val (updated, outcome) = newChallenge().makeGuess(Guess("nope"))
        assertEquals(GuessOutcome.MISS, outcome)
        assertEquals(2, updated.lives.remaining)
    }

    @Test
    fun `guessing every word beats the machine`() {
        val (afterFirst, _) = newChallenge().makeGuess(Guess("hello"))
        val (afterSecond, outcome) = afterFirst.makeGuess(Guess("world"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.BEATEN, afterSecond.status)
    }

    @Test
    fun `running out of lives loses the challenge`() {
        val (updated, _) = newChallenge(lives = 1).makeGuess(Guess("nope"))
        assertEquals(ChallengeStatus.LOST, updated.status)
    }

    @Test
    fun `a duplicate guess is a no-op and costs no life`() {
        val (afterFirst, _) = newChallenge().makeGuess(Guess("nope"))
        val (afterSecond, outcome) = afterFirst.makeGuess(Guess("Nope"))
        assertEquals(GuessOutcome.DUPLICATE, outcome)
        assertEquals(2, afterSecond.lives.remaining)
    }

    @Test
    fun `makeGuess does not mutate the receiver`() {
        val original = newChallenge()
        original.makeGuess(Guess("nope"))
        assertEquals(3, original.lives.remaining)
        assertTrue(original.guesses.isEmpty())
    }

    @Test
    fun `guessing after the challenge is over is rejected`() {
        val (lost, _) = newChallenge(lives = 1).makeGuess(Guess("nope"))
        assertFailsWith<ChallengeAlreadyOver> { lost.makeGuess(Guess("hello")) }
    }

    @Test
    fun `forfeit reveals the prompt and loses`() {
        val forfeited = newChallenge().forfeit()
        assertEquals(ChallengeStatus.LOST, forfeited.status)
        assertTrue(forfeited.maskedPrompt().tokens.all { it is MaskedToken.Revealed })
    }

    @Test
    fun `forfeit after the challenge is over is rejected`() {
        val forfeited = newChallenge().forfeit()
        assertFailsWith<ChallengeAlreadyOver> { forfeited.forfeit() }
    }

    @Test
    fun `withPicture returns an independent copy at the same version`() {
        val c = newChallenge()
        val newPicture = Picture.Ready("https://example.com/img.png")

        val updated = c.withPicture(newPicture)

        assertEquals(newPicture, updated.picture)
        // Version is untouched: the persistence adapter increments it on save.
        assertEquals(c.version, updated.version)
        // original is unchanged
        assertEquals(Picture.Pending, c.picture)
        assertNotSame(c, updated)
    }

    @Test
    fun `maxLives reflects the secret and difficulty`() {
        val challenge =
            Challenge.start(
                Prompt("a b c"),
                Lives.forSecret(Prompt("a b c"), Difficulty.MEDIUM),
                difficulty = Difficulty.MEDIUM,
            )
        assertEquals(Lives(9), challenge.maxLives())
    }
}
