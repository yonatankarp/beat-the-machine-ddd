package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.mockk.coEvery
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(StartChallengeController::class)
class StartChallengeWebContext : SpringTestConfig()

val StartChallengeControllerSuite by testSuite {
    springTest<StartChallengeWebContext> {
        val startChallenge = mockBean<StartChallenge>()

        test("POST creates a challenge and never leaks the prompt") {
            coEvery { startChallenge(any()) } returns mediumChallenge()

            bean<WebTestClient>()
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

        test("invalid difficulty query param returns 422") {
            bean<WebTestClient>()
                .post()
                .uri("/api/challenges?difficulty=NOPE")
                .exchange()
                .expectStatus()
                .isEqualTo(422)
        }
    }
}
