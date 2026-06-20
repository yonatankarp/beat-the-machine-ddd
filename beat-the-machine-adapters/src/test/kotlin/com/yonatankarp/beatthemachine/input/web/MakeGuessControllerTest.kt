package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.exception.InvalidGuess
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(MakeGuessController::class)
class MakeGuessControllerTest(
    @Autowired val client: WebTestClient,
) {
    @MockkBean
    lateinit var makeGuess: MakeGuess

    @Test
    fun `a successful guess returns the updated masked prompt`() {
        val (afterHit, _) = mediumChallenge().makeGuess("hello".asGuess())
        coEvery { makeGuess(any(), any()) } returns (afterHit to GuessOutcome.HIT)

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/guesses")
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

    @Test
    fun `guessing an unknown challenge returns 404`() {
        coEvery { makeGuess(any(), any()) } throws ChallengeNotFound(ChallengeId.new())

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/guesses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"word":"hello"}""")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `guessing on an already-over challenge returns 409`() {
        coEvery { makeGuess(any(), any()) } throws ChallengeAlreadyOver(ChallengeId.new())

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/guesses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"word":"hello"}""")
            .exchange()
            .expectStatus()
            .isEqualTo(409)
    }

    @Test
    fun `a domain-rejected guess returns 422`() {
        coEvery { makeGuess(any(), any()) } throws InvalidGuess("not a single word")

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/guesses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"word":"two words"}""")
            .exchange()
            .expectStatus()
            .isEqualTo(422)
    }

    @Test
    fun `guessing with a blank word returns 422`() {
        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/guesses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"word":"   "}""")
            .exchange()
            .expectStatus()
            .isEqualTo(422)
    }
}
