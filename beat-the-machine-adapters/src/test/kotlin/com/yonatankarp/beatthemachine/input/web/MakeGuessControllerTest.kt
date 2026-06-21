package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.mockk.coEvery
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(MakeGuessController::class)
class MakeGuessWebContext : SpringTestConfig()

val MakeGuessControllerSuite by testSuite {
    springTest<MakeGuessWebContext> {
        val makeGuess = mockBean<MakeGuess>()

        test("a successful guess returns the updated masked prompt") {
            val (afterHit, _) = mediumChallenge().makeGuess("hello".asGuess())
            coEvery { makeGuess handle any() } returns (afterHit to GuessOutcome.HIT)

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/guesses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"word":"hello"}""")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.maskedPrompt[0].revealed")
                .isEqualTo(true)
                .jsonPath("$.maskedPrompt[0].word")
                .isEqualTo("hello")
        }

        test("guessing an unknown challenge returns 404") {
            coEvery { makeGuess handle any() } throws ChallengeNotFound(aChallengeId())

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/guesses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"word":"hello"}""")
                .exchange()
                .expectStatus()
                .isNotFound
        }

        test("guessing on an already-over challenge returns 409") {
            coEvery { makeGuess handle any() } throws ChallengeAlreadyOver(aChallengeId())

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/guesses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"word":"hello"}""")
                .exchange()
                .expectStatus()
                .isEqualTo(409)
        }

        test("a domain-rejected guess returns 422") {
            coEvery { makeGuess handle any() } throws InvalidGuess("not a single word")

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/guesses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"word":"two words"}""")
                .exchange()
                .expectStatus()
                .isEqualTo(422)
        }

        test("guessing with a blank word returns 422") {
            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/guesses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"word":"   "}""")
                .exchange()
                .expectStatus()
                .isEqualTo(422)
        }
    }
}
