package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityHeadersFilterContext : SpringTestConfig()

val SecurityHeadersFilterSuite by testSuite {
    springTest<SecurityHeadersFilterContext> {
        test("every response carries the security headers") {
            bean<WebTestClient>()
                .get()
                .uri("/")
                .exchange()
                .expectHeader()
                .valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader()
                .valueEquals("Referrer-Policy", "no-referrer")
                .expectHeader()
                .value("Content-Security-Policy") { csp ->
                    csp.shouldContain("default-src 'self'")
                    csp.shouldContain("script-src 'self'")
                    csp.shouldContain("frame-ancestors 'none'")
                    csp.shouldContain("object-src 'none'")
                    csp.shouldContain("img-src 'self' https:")
                }
        }
    }
}
