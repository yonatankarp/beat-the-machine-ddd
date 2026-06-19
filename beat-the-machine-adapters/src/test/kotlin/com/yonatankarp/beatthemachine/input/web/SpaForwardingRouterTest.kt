package com.yonatankarp.beatthemachine.input.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient

class SpaForwardingRouterTest {
    private val client =
        WebTestClient
            .bindToRouterFunction(SpaForwardingRouter().spaRoutes())
            .build()

    @Test
    fun `app routes 404 harmlessly while the SPA entry point is absent`() {
        // static/index.html does not ship yet, so deep links under /app must 404, not error.
        client
            .get()
            .uri("/app/some/deep/link")
            .exchange()
            .expectStatus()
            .isNotFound
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
