package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.lostChallenge
import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.mockk.coEvery
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(ForfeitChallengeController::class)
class ForfeitChallengeWebContext : SpringTestConfig()

val ForfeitChallengeControllerSuite by testSuite {
    springTest<ForfeitChallengeWebContext> {
        val forfeitChallenge = mockBean<ForfeitChallenge>()

        test("forfeit reveals the prompt and reports LOST") {
            coEvery { forfeitChallenge handle any() } returns lostChallenge()

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/forfeit")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("LOST")
                .jsonPath("$.maskedPrompt[0].revealed")
                .isEqualTo(true)
        }

        test("forfeit with concurrent modification returns 409") {
            coEvery { forfeitChallenge handle any() } throws OptimisticLockConflict(aChallengeId())

            bean<WebTestClient>()
                .post()
                .uri("/api/challenges/${aChallengeId().value}/forfeit")
                .exchange()
                .expectStatus()
                .isEqualTo(409)
        }
    }
}
