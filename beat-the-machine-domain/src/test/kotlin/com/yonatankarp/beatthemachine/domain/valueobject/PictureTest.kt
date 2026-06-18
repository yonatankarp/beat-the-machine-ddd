package com.yonatankarp.beatthemachine.domain.valueobject

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PictureTest {
    @Test
    fun `Pending is a Picture`() {
        assertIs<Picture.Pending>(Picture.Pending)
    }

    @Test
    fun `Ready carries url`() {
        val pic = Picture.Ready("https://example.com/img.png")
        assertIs<Picture.Ready>(pic)
        assertEquals("https://example.com/img.png", pic.url)
    }

    @Test
    fun `Failed is a Picture`() {
        assertIs<Picture.Failed>(Picture.Failed)
    }
}
