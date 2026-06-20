package com.yonatankarp.beatthemachine.input.web

import com.ninjasquad.springmockk.MockkBean
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(PictureController::class)
class PictureControllerTest(
    @Autowired val client: WebTestClient,
) {
    @MockkBean
    lateinit var pictureStore: PictureStore

    @Test
    fun `serves stored bytes with content type`() {
        coEvery { pictureStore.load("abc") } returns StoredImage(byteArrayOf(1, 2, 3), "image/png")

        client
            .get()
            .uri("/images/abc")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("image/png")
            .expectBody()
            .consumeWith { assert(it.responseBody!!.contentEquals(byteArrayOf(1, 2, 3))) }
    }

    @Test
    fun `unknown id returns 404`() {
        coEvery { pictureStore.load("missing") } returns null

        client
            .get()
            .uri("/images/missing")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `non-image content type is coerced to application octet-stream`() {
        coEvery { pictureStore.load("malicious") } returns
            StoredImage(
                byteArrayOf(60, 104, 116, 109, 108, 62),
                "text/html",
            )

        client
            .get()
            .uri("/images/malicious")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/octet-stream")
            .expectBody()
            .consumeWith { assert(it.responseBody!!.contentEquals(byteArrayOf(60, 104, 116, 109, 108, 62))) }
    }
}
