package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeStatus
import com.yonatankarp.beatthemachine.openapi.v1.models.Difficulty
import com.yonatankarp.beatthemachine.openapi.v1.models.MaskedToken
import com.yonatankarp.beatthemachine.openapi.v1.models.PictureStatus
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty

class ChallengeApiMapperTest {
    private fun challenge() = Challenge.start(Prompt("hello world"), Lives(6))

    @Test
    fun `maps a pending picture`() {
        // Given
        val challenge = challenge()

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(PictureStatus.PENDING, r.picture.status)
        assertEquals(null, r.picture.url)
    }

    @Test
    fun `maps a ready picture with its url`() {
        // Given
        val challenge = challenge().withPicture(Picture.Ready("http://img/1.png"))

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(PictureStatus.READY, r.picture.status)
        assertEquals(URI.create("http://img/1.png"), r.picture.url)
    }

    @Test
    fun `maps a failed picture`() {
        // Given
        val challenge = challenge().withPicture(Picture.Failed)

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, r.picture.status)
        assertEquals(null, r.picture.url)
    }

    @Test
    fun `degrades a ready picture with a malformed url to FAILED`() {
        // Given
        val challenge = challenge().withPicture(Picture.Ready("http:// bad url with spaces"))

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, r.picture.status)
        assertEquals(null, r.picture.url)
    }

    @Test
    fun `hides every word while the challenge is in progress`() {
        // Given
        val challenge = challenge()

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(false, null, 5), MaskedToken(false, null, 5)), r.maskedPrompt)
        assertEquals(ChallengeStatus.IN_PROGRESS, r.status)
    }

    @Test
    fun `reveals the whole prompt once the challenge is lost`() {
        // Given
        val challenge = challenge().forfeit()

        // When
        val r = challenge.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5)), r.maskedPrompt)
        assertEquals("LOST", r.status.value)
    }

    @Test
    fun `maps livesRemaining and the difficulty-derived maxLives`() {
        // Given
        val prompt = Prompt("hello world")
        val mediumChallenge = challenge()
        val easyChallenge =
            Challenge
                .start(
                    prompt,
                    Lives.forSecret(prompt, DomainDifficulty.EASY),
                    difficulty = DomainDifficulty.EASY,
                )

        // When
        val medium = mediumChallenge.toApiResponse()
        val easy = easyChallenge.toApiResponse()

        // Then
        assertEquals(6, medium.livesRemaining)
        assertEquals(Lives.forSecret(prompt, DomainDifficulty.MEDIUM).remaining, medium.maxLives)
        assertEquals(Lives.forSecret(prompt, DomainDifficulty.EASY).remaining, easy.maxLives)
    }

    @Test
    fun `Difficulty toDomain maps each value to the same-named domain Difficulty`() {
        // Given
        val easy = Difficulty.EASY
        val medium = Difficulty.MEDIUM
        val hard = Difficulty.HARD

        // When
        val easyDomain = easy.toDomain()
        val mediumDomain = medium.toDomain()
        val hardDomain = hard.toDomain()

        // Then
        assertEquals(DomainDifficulty.EASY, easyDomain)
        assertEquals(DomainDifficulty.MEDIUM, mediumDomain)
        assertEquals(DomainDifficulty.HARD, hardDomain)
    }
}
