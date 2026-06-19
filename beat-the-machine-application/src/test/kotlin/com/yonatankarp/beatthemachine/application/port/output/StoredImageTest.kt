package com.yonatankarp.beatthemachine.application.port.output

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StoredImageTest {
    @Test
    fun `equal by content`() {
        assertEquals(
            StoredImage(byteArrayOf(1, 2, 3), "image/png"),
            StoredImage(byteArrayOf(1, 2, 3), "image/png"),
        )
    }
}
