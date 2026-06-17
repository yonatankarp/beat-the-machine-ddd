package com.yonatankarp.beatthemachine

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class HealthEndpointTest(
    @Autowired val rest: TestRestTemplate,
) {
    @Test
    fun `health endpoint reports UP`() {
        val body = rest.getForObject("/health", String::class.java)
        val nonNullBody = requireNotNull(body) { "Response body must not be null" }
        assertTrue(nonNullBody.contains("\"status\":\"UP\""))
    }
}
