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
            val url = store.save(byteArrayOf(9, 8, 7), "image/png")
            assertTrue(url.startsWith("/images/"))
            val id = url.removePrefix("/images/")
            val loaded = store.load(id)!!
            assertEquals("image/png", loaded.contentType)
            assertTrue(byteArrayOf(9, 8, 7).contentEquals(loaded.bytes))
        }

    @Test
    fun `load returns null for unknown id`() =
        runTest {
            assertNull(store.load("nope"))
        }
}
