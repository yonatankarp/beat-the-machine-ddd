package com.yonatankarp.beatthemachine.output.persistence.inmemory

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryPictureStoreTest {
    @Test
    fun `save returns an images url and load round-trips`() =
        runTest {
            // Given
            val store = InMemoryPictureStore()
            val bytes = byteArrayOf(9, 8, 7)
            val contentType = "image/png"

            // When
            val url = store.save(bytes, contentType)

            // Then
            assertTrue(url.startsWith("/images/"))
            val id = url.removePrefix("/images/")
            val loaded = store.load(id)!!
            assertEquals("image/png", loaded.contentType)
            assertTrue(byteArrayOf(9, 8, 7).contentEquals(loaded.bytes))
        }

    @Test
    fun `load returns null for unknown id`() =
        runTest {
            // Given
            val store = InMemoryPictureStore()
            val unknownId = "nope"

            // When
            val loaded = store.load(unknownId)

            // Then
            assertNull(loaded)
        }
}
