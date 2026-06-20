package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryStorePictureTest {
    private val storage = InMemoryPictureStorage()
    private val storePicture = InMemoryStorePicture(storage)

    @Test
    fun `handle returns an id and stores the bytes`() =
        runTest {
            // Given
            val bytes = byteArrayOf(9, 8, 7)

            // When
            val id = storePicture handle StorePicture.Command(bytes, "image/png")

            // Then
            assertTrue(id.isNotBlank())
            val stored = storage.byId[id]!!
            assertEquals("image/png", stored.contentType)
            assertTrue(bytes.contentEquals(stored.bytes))
        }
}
