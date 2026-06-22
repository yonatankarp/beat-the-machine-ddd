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
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import java.net.URI
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty as DomainDifficulty

val ChallengeApiMapperSuite by testSuite {
    given("a challenge domain object") {
        whenever("mapped to the API model") {
            then("it maps a pending picture") {
                val subject = mediumChallenge()
                val response = subject.toApiResponse()
                response.picture.status shouldBe PictureStatus.PENDING
                response.picture.url shouldBe null
            }

            then("it maps a ready picture with its url") {
                val subject = mediumChallenge().withPicture(readyPicture("http://img/1.png"))
                val response = subject.toApiResponse()
                response.picture.status shouldBe PictureStatus.READY
                response.picture.url shouldBe URI.create("http://img/1.png")
            }

            then("it maps a failed picture") {
                val subject = mediumChallenge().withPicture(failedPicture())
                val response = subject.toApiResponse()
                response.picture.status shouldBe PictureStatus.FAILED
                response.picture.url shouldBe null
            }

            then("it degrades a ready picture with a malformed url to FAILED") {
                val subject = mediumChallenge().withPicture(readyPicture("http:// bad url with spaces"))
                val response = subject.toApiResponse()
                response.picture.status shouldBe PictureStatus.FAILED
                response.picture.url shouldBe null
            }

            then("it maps livesRemaining and the prompt-derived maxLives") {
                val mediumChallenge = mediumChallenge()
                val easyChallenge = easyChallenge()
                val medium = mediumChallenge.toApiResponse()
                val easy = easyChallenge.toApiResponse()
                medium.livesRemaining shouldBe 6
                medium.maxLives shouldBe mediumChallenge.maxLives().remaining
                easy.maxLives shouldBe easyChallenge.maxLives().remaining
            }
        }

        whenever("the challenge is in progress") {
            then("it hides every word") {
                val subject = mediumChallenge()
                val response = subject.toApiResponse()
                response.maskedPrompt shouldBe listOf(MaskedToken(false, null, 5), MaskedToken(false, null, 5))
                response.status shouldBe ChallengeStatus.IN_PROGRESS
            }
        }

        whenever("the challenge is lost") {
            then("it reveals the whole prompt") {
                val subject = lostChallenge()
                val response = subject.toApiResponse()
                response.maskedPrompt shouldBe listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5))
                response.status.value shouldBe "LOST"
            }
        }

        whenever("the challenge is beaten") {
            then("it reveals the whole prompt") {
                val subject = beatenChallenge()
                val response = subject.toApiResponse()
                response.maskedPrompt shouldBe listOf(MaskedToken(true, "hello", 5), MaskedToken(true, "world", 5))
                response.status.value shouldBe "BEATEN"
            }
        }
    }

    given("an API Difficulty value") {
        whenever("mapped to the domain model") {
            then("it maps each value to the same-named domain Difficulty") {
                Difficulty.EASY.toDomain() shouldBe DomainDifficulty.EASY
                Difficulty.MEDIUM.toDomain() shouldBe DomainDifficulty.MEDIUM
                Difficulty.HARD.toDomain() shouldBe DomainDifficulty.HARD
            }
        }
    }
}
