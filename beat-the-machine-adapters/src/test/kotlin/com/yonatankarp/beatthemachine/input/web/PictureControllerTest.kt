package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import com.yonatankarp.testballoon.spring.SpringTestConfig
import com.yonatankarp.testballoon.spring.springTest
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(PictureController::class)
class PictureWebContext : SpringTestConfig()

val PictureControllerSuite by testSuite {
    springTest<PictureWebContext> {
        val findPicture = mockBean<FindPicture>()

        test("serves image bytes with correct content-type") {
            coEvery { findPicture answer FindPicture.Query("abc") } returns StoredImage(byteArrayOf(1, 2, 3), "image/png")

            val response =
                bean<WebTestClient>()
                    .get()
                    .uri("/images/abc")
                    .exchange()

            response
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType("image/png")
                .expectHeader()
                .valueEquals("Cache-Control", "public, max-age=31536000, immutable")
                .expectBody<ByteArray>()
                .consumeWith { it.responseBody shouldBe byteArrayOf(1, 2, 3) }
        }

        test("returns 404 for unknown id") {
            coEvery { findPicture answer FindPicture.Query("unknown") } returns null

            val response =
                bean<WebTestClient>()
                    .get()
                    .uri("/images/unknown")
                    .exchange()

            response
                .expectStatus()
                .isNotFound
        }

        test("coerces non-image content-type to application octet-stream") {
            coEvery { findPicture answer FindPicture.Query("abc") } returns StoredImage(byteArrayOf(1, 2, 3), "text/plain")

            val response =
                bean<WebTestClient>()
                    .get()
                    .uri("/images/abc")
                    .exchange()

            response
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType("application/octet-stream")
        }
    }
}
