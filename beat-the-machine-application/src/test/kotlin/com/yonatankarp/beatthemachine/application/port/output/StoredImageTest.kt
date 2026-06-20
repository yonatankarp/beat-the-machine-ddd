package com.yonatankarp.beatthemachine.application.port.output

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StoredImageTest {
    @Test
    fun `equal by content`() {
        // Given
        val left = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val right = StoredImage(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertEquals(left, right)
    }
}
