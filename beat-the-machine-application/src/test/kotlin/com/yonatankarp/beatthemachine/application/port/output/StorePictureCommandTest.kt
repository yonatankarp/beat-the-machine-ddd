package com.yonatankarp.beatthemachine.application.port.output

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StorePictureCommandTest {
    @Test
    fun `commands with the same bytes and content type are equal`() {
        // Given
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val same = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertEquals(command, same)
        assertEquals(command.hashCode(), same.hashCode())
    }

    @Test
    fun `a command equals itself`() {
        // Given
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertTrue(command.equals(command))
    }

    @Test
    fun `a command does not equal a value of another type`() {
        // Given
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")

        // Then
        assertFalse(command.equals("image/png"))
    }

    @Test
    fun `commands differing in content type are not equal`() {
        // Given
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val other = StorePicture.Command(byteArrayOf(1, 2, 3), "image/jpeg")

        // Then
        assertNotEquals(command, other)
    }

    @Test
    fun `commands differing in bytes are not equal`() {
        // Given
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val other = StorePicture.Command(byteArrayOf(9, 9, 9), "image/png")

        // Then
        assertNotEquals(command, other)
    }
}
