package com.yonatankarp.beatthemachine.domain

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
        val c = newChallenge()
        val outcome = c.makeGuess(Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.IN_PROGRESS, c.status)
        assertEquals(3, c.lives.remaining)
    }

    @Test
    fun `a wrong guess costs a life`() {
        val c = newChallenge()
        val outcome = c.makeGuess(Guess("nope"))
        assertEquals(GuessOutcome.MISS, outcome)
        assertEquals(2, c.lives.remaining)
    }

    @Test
    fun `guessing every word beats the machine`() {
        val c = newChallenge()
        c.makeGuess(Guess("hello"))
        val outcome = c.makeGuess(Guess("world"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(ChallengeStatus.BEATEN, c.status)
    }

    @Test
    fun `running out of lives loses the challenge`() {
        val c = newChallenge(lives = 1)
        c.makeGuess(Guess("nope"))
        assertEquals(ChallengeStatus.LOST, c.status)
    }

    @Test
    fun `a duplicate guess is a no-op and costs no life`() {
        val c = newChallenge()
        c.makeGuess(Guess("nope"))
        val outcome = c.makeGuess(Guess("Nope"))
        assertEquals(GuessOutcome.DUPLICATE, outcome)
        assertEquals(2, c.lives.remaining)
    }

    @Test
    fun `guessing after the challenge is over is rejected`() {
        val c = newChallenge(lives = 1)
        c.makeGuess(Guess("nope")) // lost
        assertFailsWith<ChallengeAlreadyOver> { c.makeGuess(Guess("hello")) }
    }

    @Test
    fun `forfeit reveals the prompt and loses`() {
        val c = newChallenge()
        c.forfeit()
        assertEquals(ChallengeStatus.LOST, c.status)
        assertTrue(c.maskedPrompt().tokens.all { it is MaskedToken.Revealed })
    }

    @Test
    fun `withPicture returns a copy with new picture and version bumped`() {
        val c = newChallenge()
        val originalVersion = c.version
        val originalPicture = c.picture
        val newPicture = Picture.Ready("https://example.com/img.png")

        val updated = c.withPicture(newPicture)

        assertEquals(newPicture, updated.picture)
        assertEquals(originalVersion + 1, updated.version)
        // original is unchanged
        assertEquals(originalPicture, c.picture)
        assertEquals(originalVersion, c.version)
        assertNotSame(c, updated)
    }
}
