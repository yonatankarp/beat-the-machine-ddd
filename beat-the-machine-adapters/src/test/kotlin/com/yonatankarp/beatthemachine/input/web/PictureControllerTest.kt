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
    fun `serves image bytes with correct content-type`() {
        // Given
        coEvery { pictureStore.load("abc") } returns StoredImage(byteArrayOf(1, 2, 3), "image/png")

        // When
        val response =
            client
                .get()
                .uri("/images/abc")
                .exchange()

        // Then
        response
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("image/png")
            .expectHeader()
            .valueEquals("Cache-Control", "public, max-age=31536000, immutable")
            .expectBody()
            .consumeWith { assert(it.responseBody!!.contentEquals(byteArrayOf(1, 2, 3))) }
    }

    @Test
    fun `returns 404 for unknown id`() {
        // Given
        coEvery { pictureStore.load("unknown") } returns null

        // When
        val response =
            client
                .get()
                .uri("/images/unknown")
                .exchange()

        // Then
        response
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `coerces non-image content-type to application octet-stream`() {
        // Given
        coEvery { pictureStore.load("abc") } returns StoredImage(byteArrayOf(1, 2, 3), "text/plain")

        // When
        val response =
            client
                .get()
                .uri("/images/abc")
                .exchange()

        // Then
        response
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/octet-stream")
    }
}
