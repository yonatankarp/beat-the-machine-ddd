package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PictureTest {
    @Test
    fun `Pending is a Picture`() {
        // When
        val picture = Picture.Pending

        // Then
        assertIs<Picture.Pending>(picture)
    }

    @Test
    fun `Ready carries url`() {
        // Given
        val url = "https://example.com/img.png"

        // When
        val pic = Picture.Ready(url)

        // Then
        assertIs<Picture.Ready>(pic)
        assertEquals("https://example.com/img.png", pic.url)
    }

    @Test
    fun `Failed is a Picture`() {
        // When
        val picture = Picture.Failed

        // Then
        assertIs<Picture.Failed>(picture)
    }
}
