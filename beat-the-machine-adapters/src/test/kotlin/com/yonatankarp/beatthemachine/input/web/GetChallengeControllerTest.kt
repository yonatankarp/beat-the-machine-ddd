package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(GetChallengeController::class)
class GetChallengeControllerTest(
    @Autowired val client: WebTestClient,
) {
    @MockkBean
    lateinit var getChallenge: GetChallenge

    @Test
    fun `GET returns the challenge state`() {
        val challenge = mediumChallenge()
        coEvery { getChallenge(any()) } returns challenge

        client
            .get()
            .uri("/api/challenges/${challenge.id.value}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("IN_PROGRESS")
            .jsonPath("$.livesRemaining")
            .isEqualTo(6)
    }

    @Test
    fun `GET an unknown challenge returns 404`() {
        coEvery { getChallenge(any()) } throws ChallengeNotFound(aChallengeId())

        client
            .get()
            .uri("/api/challenges/${aChallengeId().value}")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `a malformed challenge id returns 422`() {
        client
            .get()
            .uri("/api/challenges/not-a-uuid")
            .exchange()
            .expectStatus()
            .isEqualTo(422)
    }
}
