package com.yonatankarp.beatthemachine.output.persistence.inmemory

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryPictureStoreTest {
    private val store = InMemoryPictureStore()

    @Test
    fun `save returns an images url and load round-trips`() =
        runTest {
            // Given
            val bytes = byteArrayOf(9, 8, 7)

            // When
            val url = store.save(bytes, "image/png")

            // Then
            assertTrue(url.startsWith("/images/"))
            val id = url.removePrefix("/images/")
            val loaded = store.load(id)!!
            assertEquals("image/png", loaded.contentType)
            assertTrue(bytes.contentEquals(loaded.bytes))
        }

    @Test
    fun `load returns null for unknown id`() =
        runTest {
            // When / Then
            assertNull(store.load("nope"))
        }
}
