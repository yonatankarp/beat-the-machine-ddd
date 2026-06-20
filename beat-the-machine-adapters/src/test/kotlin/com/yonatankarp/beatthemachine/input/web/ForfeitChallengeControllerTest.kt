package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(ForfeitChallengeController::class)
class ForfeitChallengeControllerTest(
    @Autowired val client: WebTestClient,
) {
    @MockkBean
    lateinit var forfeitChallenge: ForfeitChallenge

    @Test
    fun `forfeit reveals the prompt and reports LOST`() {
        coEvery { forfeitChallenge(any()) } returns Challenge.start(Prompt("hello world"), Lives(6)).forfeit()

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/forfeit")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("LOST")
            .jsonPath("$.maskedPrompt[0].revealed")
            .isEqualTo(true)
    }

    @Test
    fun `forfeit with concurrent modification returns 409`() {
        coEvery { forfeitChallenge(any()) } throws OptimisticLockConflict(ChallengeId.new())

        client
            .post()
            .uri("/api/challenges/${ChallengeId.new().value}/forfeit")
            .exchange()
            .expectStatus()
            .isEqualTo(409)
    }
}
