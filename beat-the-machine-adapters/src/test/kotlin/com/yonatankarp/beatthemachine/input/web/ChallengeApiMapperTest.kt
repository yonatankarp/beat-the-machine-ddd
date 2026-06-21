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
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import java.net.URI
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty

val ChallengeApiMapperSuite by testSuite {
    test("maps a pending picture") {
        // Given
        val subject = mediumChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        response.picture.status shouldBe PictureStatus.PENDING
        response.picture.url shouldBe null
    }

    test("maps a ready picture with its url") {
        // Given
        val subject = mediumChallenge().withPicture(readyPicture("http://img/1.png"))

        // When
        val response = subject.toApiResponse()

        // Then
        response.picture.status shouldBe PictureStatus.READY
        response.picture.url shouldBe URI.create("http://img/1.png")
    }

    test("maps a failed picture") {
        // Given
        val subject = mediumChallenge().withPicture(failedPicture())

        // When
        val response = subject.toApiResponse()

        // Then
        response.picture.status shouldBe PictureStatus.FAILED
        response.picture.url shouldBe null
    }

    test("degrades a ready picture with a malformed url to FAILED") {
        // Given
        val subject = mediumChallenge().withPicture(readyPicture("http:// bad url with spaces"))

        // When
        val response = subject.toApiResponse()

        // Then
        response.picture.status shouldBe PictureStatus.FAILED
        response.picture.url shouldBe null
    }

    test("hides every word while the challenge is in progress") {
        // Given
        val subject = mediumChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        response.maskedPrompt shouldBe listOf(MaskedToken(false, null, 5), MaskedToken(false, null, 5))
        response.status shouldBe ChallengeStatus.IN_PROGRESS
    }

    test("reveals the whole prompt once the challenge is lost") {
        // Given
        val subject = lostChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        response.maskedPrompt shouldBe listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5))
        response.status.value shouldBe "LOST"
    }

    test("reveals the whole prompt once the challenge is beaten") {
        // Given
        val subject = beatenChallenge()

        // When
        val response = subject.toApiResponse()

        // Then
        response.maskedPrompt shouldBe listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5))
        response.status.value shouldBe "BEATEN"
    }

    test("maps livesRemaining and the difficulty-derived maxLives") {
        // Given
        val mediumChallenge = mediumChallenge()
        val easyChallenge = easyChallenge()

        // When
        val medium = mediumChallenge.toApiResponse()
        val easy = easyChallenge.toApiResponse()

        // Then
        medium.livesRemaining shouldBe 6
        medium.maxLives shouldBe 6
        easy.maxLives shouldBe 8
    }

    test("Difficulty toDomain maps each value to the same-named domain Difficulty") {
        // When
        val easy = Difficulty.EASY.toDomain()
        val medium = Difficulty.MEDIUM.toDomain()
        val hard = Difficulty.HARD.toDomain()

        // Then
        easy shouldBe DomainDifficulty.EASY
        medium shouldBe DomainDifficulty.MEDIUM
        hard shouldBe DomainDifficulty.HARD
    }
}
