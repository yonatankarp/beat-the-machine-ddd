package com.yonatankarp.beatthemachine.input.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

class SpaForwardingRouterTest {
    private val client =
        WebTestClient
            .bindToRouterFunction(SpaForwardingRouter().spaRoutes())
            .build()

    @Test
    fun `app deep links are served by the SPA entry point`() {
        // static/index.html is now bundled; deep links under /app must return the SPA.
        client
            .get()
            .uri("/app/some/deep/link")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `the router is scoped to app and does not match other paths`() {
        // A path outside /app/** must not be handled by this router (it falls through to a 404
        // from the router with no matching route), proving it never shadows /api or /actuator.
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
class SpaForwardingRouterIntegrationTest(
    @Autowired private val client: WebTestClient,
) {
    @Test
    fun `root redirects to the SPA entry`() {
        client
            .get()
            .uri("/")
            .exchange()
            .expectStatus()
            .isFound
            .expectHeader()
            .valueEquals("Location", "/app/")
    }
}
