package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PictureController(
    private val pictureStore: PictureStore,
) {
    @GetMapping("/images/{id}")
    suspend fun image(
        @PathVariable id: String,
    ): ResponseEntity<ByteArray> {
        val image = pictureStore.load(id) ?: return ResponseEntity.notFound().build()
        val safeContentType =
            if (image.contentType in ALLOWED_CONTENT_TYPES) {
                image.contentType
            } else {
                FALLBACK_CONTENT_TYPE
            }
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(safeContentType))
            .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE)
            .body(image.bytes)
    }

    private companion object {
        val ALLOWED_CONTENT_TYPES = setOf("image/png", "image/jpeg", "image/webp", "image/gif")
        const val FALLBACK_CONTENT_TYPE = "application/octet-stream"
        const val MAX_AGE_SECONDS = 31536000
        const val CACHE_CONTROL_VALUE = "public, max-age=$MAX_AGE_SECONDS, immutable"
    }
}
