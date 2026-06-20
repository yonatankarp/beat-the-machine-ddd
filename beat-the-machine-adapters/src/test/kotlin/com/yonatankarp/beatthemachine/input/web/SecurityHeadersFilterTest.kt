package com.yonatankarp.beatthemachine.input.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertContains

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
                assertContains(csp, "default-src 'self'")
                assertContains(csp, "script-src 'self'")
                assertContains(csp, "frame-ancestors 'none'")
                assertContains(csp, "object-src 'none'")
                assertContains(csp, "img-src 'self' https:")
            }
    }
}
