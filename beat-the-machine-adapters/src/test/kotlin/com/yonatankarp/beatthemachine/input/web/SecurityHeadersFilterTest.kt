package com.yonatankarp.beatthemachine.input.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityHeadersFilterTest(
    @Autowired private val client: WebTestClient,
) {
    @Test
    fun `every response carries the security headers`() {
        client
            .get()
            .uri("/")
            .exchange()
            .expectHeader()
            .valueEquals("X-Content-Type-Options", "nosniff")
            .expectHeader()
            .valueEquals("Referrer-Policy", "no-referrer")
            .expectHeader()
            .value("Content-Security-Policy") { csp ->
                require("default-src 'self'" in csp) { "missing default-src: $csp" }
                require("script-src 'self'" in csp) { "missing script-src: $csp" }
                require("frame-ancestors 'none'" in csp) { "missing frame-ancestors: $csp" }
                require("object-src 'none'" in csp) { "missing object-src: $csp" }
                require("img-src 'self' https:" in csp) { "missing img-src: $csp" }
            }
    }
}
