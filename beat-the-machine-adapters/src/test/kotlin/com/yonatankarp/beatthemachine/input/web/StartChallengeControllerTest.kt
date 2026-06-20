package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(StartChallengeController::class)
@MockkBean(types = [StartChallenge::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class StartChallengeControllerTest(
    private val client: WebTestClient,
    private val startChallenge: StartChallenge,
) {
    @Test
    fun `POST creates a challenge and never leaks the prompt`() {
        coEvery { startChallenge handle any() } returns mediumChallenge()

        client
            .post()
            .uri("/api/challenges")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.livesRemaining")
            .isEqualTo(6)
            .jsonPath("$.status")
            .isEqualTo("IN_PROGRESS")
            .jsonPath("$.picture.status")
            .isEqualTo("PENDING")
            .jsonPath("$.maskedPrompt[0].revealed")
            .isEqualTo(false)
            .jsonPath("$.prompt")
            .doesNotExist()
            .jsonPath("$.secretPrompt")
            .doesNotExist()
    }

    @Test
    fun `invalid difficulty query param returns 422`() {
        client
            .post()
            .uri("/api/challenges?difficulty=NOPE")
            .exchange()
            .expectStatus()
            .isEqualTo(422)
    }
}
