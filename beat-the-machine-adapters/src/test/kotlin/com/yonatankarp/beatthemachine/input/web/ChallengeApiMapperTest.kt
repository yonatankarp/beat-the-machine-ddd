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
        val subject = challenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.PENDING, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `maps a ready picture with its url`() {
        // Given
        val subject = challenge().withPicture(Picture.Ready("http://img/1.png"))

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.READY, response.picture.status)
        assertEquals(URI.create("http://img/1.png"), response.picture.url)
    }

    @Test
    fun `maps a failed picture`() {
        // Given
        val subject = challenge().withPicture(Picture.Failed)

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `degrades a ready picture with a malformed url to FAILED`() {
        // Given
        val subject = challenge().withPicture(Picture.Ready("http:// bad url with spaces"))

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `hides every word while the challenge is in progress`() {
        // Given
        val subject = challenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(false, null, 5), MaskedToken(false, null, 5)), response.maskedPrompt)
        assertEquals(ChallengeStatus.IN_PROGRESS, response.status)
    }

    @Test
    fun `reveals the whole prompt once the challenge is lost`() {
        // Given
        val subject = challenge().forfeit()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5)), response.maskedPrompt)
        assertEquals("LOST", response.status.value)
    }

    @Test
    fun `maps livesRemaining and the difficulty-derived maxLives`() {
        // Given
        val mediumChallenge = challenge()
        val easyChallenge = Challenge.start(Prompt("hello world"), Lives(8), difficulty = DomainDifficulty.EASY)

        // When
        val medium = mediumChallenge.toApiResponse()
        val easy = easyChallenge.toApiResponse()

        // Then
        assertEquals(6, medium.livesRemaining)
        assertEquals(6, medium.maxLives)
        assertEquals(8, easy.maxLives)
    }

    @Test
    fun `Difficulty toDomain maps each value to the same-named domain Difficulty`() {
        // When
        val easy = Difficulty.EASY.toDomain()
        val medium = Difficulty.MEDIUM.toDomain()
        val hard = Difficulty.HARD.toDomain()

        // Then
        assertEquals(DomainDifficulty.EASY, easy)
        assertEquals(DomainDifficulty.MEDIUM, medium)
        assertEquals(DomainDifficulty.HARD, hard)
    }
}
