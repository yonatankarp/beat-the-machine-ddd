package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.openapi.v1.models.ChallengeStatus
import com.yonatankarp.beatthemachine.openapi.v1.models.Difficulty
import com.yonatankarp.beatthemachine.openapi.v1.models.MaskedToken
import com.yonatankarp.beatthemachine.openapi.v1.models.PictureStatus
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.beatenChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.easyChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.lostChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty

class ChallengeApiMapperTest {
    @Test
    fun `maps a pending picture`() {
        // Given
        val subject = mediumChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.PENDING, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `maps a ready picture with its url`() {
        // Given
        val subject = mediumChallenge().withPicture(readyPicture("http://img/1.png"))

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.READY, response.picture.status)
        assertEquals(URI.create("http://img/1.png"), response.picture.url)
    }

    @Test
    fun `maps a failed picture`() {
        // Given
        val subject = mediumChallenge().withPicture(failedPicture())

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `degrades a ready picture with a malformed url to FAILED`() {
        // Given
        val subject = mediumChallenge().withPicture(readyPicture("http:// bad url with spaces"))

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(PictureStatus.FAILED, response.picture.status)
        assertEquals(null, response.picture.url)
    }

    @Test
    fun `hides every word while the challenge is in progress`() {
        // Given
        val subject = mediumChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(false, null, 5), MaskedToken(false, null, 5)), response.maskedPrompt)
        assertEquals(ChallengeStatus.IN_PROGRESS, response.status)
    }

    @Test
    fun `reveals the whole prompt once the challenge is lost`() {
        // Given
        val subject = lostChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5)), response.maskedPrompt)
        assertEquals("LOST", response.status.value)
    }

    @Test
    fun `reveals the whole prompt once the challenge is beaten`() {
        // Given
        val subject = beatenChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        assertEquals(listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5)), response.maskedPrompt)
        assertEquals("BEATEN", response.status.value)
    }

    @Test
    fun `maps livesRemaining and the difficulty-derived maxLives`() {
        // Given
        val mediumChallenge = mediumChallenge()
        val easyChallenge = easyChallenge()

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
