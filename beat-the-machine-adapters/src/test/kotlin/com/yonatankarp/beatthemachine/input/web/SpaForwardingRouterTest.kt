package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

val SpaForwardingRouterUnitSuite by testSuite {
    test("the router is scoped to app and does not match other paths") {
        val client =
            WebTestClient
                .bindToRouterFunction(SpaForwardingRouter().spaRoutes())
                .build()
        client
            .get()
            .uri("/api/challenges/anything")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SpaForwardingRouterContext : SpringTestConfig()

val SpaForwardingRouterIntegrationSuite by testSuite {
    springTest<SpaForwardingRouterContext> {
        test("root redirects to the SPA entry") {
            bean<WebTestClient>()
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isFound
                .expectHeader()
                .valueEquals("Location", "/app/")
        }

        test("app deep links are served by the SPA entry point") {
            bean<WebTestClient>()
                .get()
                .uri("/app/some/deep/link")
                .exchange()
                .expectStatus()
                .isOk
        }
    }
}
