package com.yonatankarp.beatthemachine.application.port.output

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StoredImageTest {
    @Test
    fun `images with the same bytes and content type are equal`() {
        // Given
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val same = StoredImage(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertEquals(image, same)
        assertEquals(image.hashCode(), same.hashCode())
    }

    @Test
    fun `an image equals itself`() {
        // Given
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertTrue(image.equals(image))
    }

    @Test
    fun `an image does not equal a value of another type`() {
        // Given
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertFalse(image.equals("image/png"))
    }

    @Test
    fun `images differing in content type are not equal`() {
        // Given
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val other = StoredImage(byteArrayOf(1, 2, 3), "image/jpeg")

        // Then
        assertNotEquals(image, other)
    }

    @Test
    fun `images differing in bytes are not equal`() {
        // Given
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val other = StoredImage(byteArrayOf(9, 9, 9), "image/png")

        // Then
        assertNotEquals(image, other)
    }
}
