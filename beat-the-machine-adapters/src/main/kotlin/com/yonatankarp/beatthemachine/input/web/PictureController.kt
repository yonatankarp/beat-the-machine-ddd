package com.yonatankarp.beatthemachine.input.web

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
class PictureController(
    private val pictureStore: PictureStore,
) {
    @GetMapping("/images/{id}")
    suspend fun image(
        @PathVariable id: String,
    ): ResponseEntity<ByteArray> {
        val image = pictureStore.load(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
            .body(image.bytes)
    }
}
