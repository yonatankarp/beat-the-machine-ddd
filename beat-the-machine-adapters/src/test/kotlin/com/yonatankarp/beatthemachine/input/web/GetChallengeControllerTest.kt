package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.mockk.coEvery
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(GetChallengeController::class)
class GetChallengeWebContext : SpringTestConfig()

val GetChallengeControllerSuite by testSuite {
    springTest<GetChallengeWebContext> {
        val getChallenge = mockBean<GetChallenge>()

        test("GET returns the challenge state") {
            val challenge = mediumChallenge()
            coEvery { getChallenge(any()) } returns challenge

            bean<WebTestClient>()
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

        test("GET an unknown challenge returns 404") {
            coEvery { getChallenge(any()) } throws ChallengeNotFound(aChallengeId())

            bean<WebTestClient>()
                .get()
                .uri("/api/challenges/${aChallengeId().value}")
                .exchange()
                .expectStatus()
                .isNotFound
        }

        test("a malformed challenge id returns 422") {
            bean<WebTestClient>()
                .get()
                .uri("/api/challenges/not-a-uuid")
                .exchange()
                .expectStatus()
                .isEqualTo(422)
        }
    }
}
