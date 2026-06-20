package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryFindPictureTest {
    private val storage = InMemoryPictureStorage()
    private val storePicture = InMemoryStorePicture(storage)
    private val findPicture = InMemoryFindPicture(storage)

    @Test
    fun `round-trips bytes and content type`() =
        runTest {
            // Given
            val bytes = byteArrayOf(9, 8, 7)

            // When
            val id = storePicture handle StorePicture.Command(bytes, "image/png")
            val loaded = (findPicture answer FindPicture.Query(id))!!

            // Then
            assertEquals("image/png", loaded.contentType)
            assertTrue(bytes.contentEquals(loaded.bytes))
        }

    @Test
    fun `answer returns null for unknown id`() =
        runTest {
            // When / Then
            assertNull(findPicture answer FindPicture.Query("nope"))
        }
}
