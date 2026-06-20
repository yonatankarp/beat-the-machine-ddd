package com.yonatankarp.beatthemachine.output.persistence.sqlite

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlitePictureStoreIT {
    private val jdbc = newSqliteJdbc()
    private val store = SqlitePictureStore(jdbc)

    @Test
    fun `save then load round-trips the bytes and content type`() =
        runTest {
            // Given
            val bytes = byteArrayOf(1, 2, 3, 4)

            // When
            val url = store.save(bytes, "image/png")
            val id = url.removePrefix("/images/")
            val loaded = store.load(id)!!

            // Then
            assertEquals("image/png", loaded.contentType)
            assertTrue(bytes.contentEquals(loaded.bytes))
        }

    @Test
    fun `bytes live in the picture table, not on challenge`() =
        runTest {
            // Given / When
            store.save(byteArrayOf(1), "image/png")

            // Then
            val pictureRows = jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
            val challengeRows = jdbc.queryForObject("SELECT COUNT(*) FROM challenge", Int::class.java)
            assertEquals(1, pictureRows)
            assertEquals(0, challengeRows)
        }

    @Test
    fun `load returns null for unknown id`() =
        runTest {
            // When / Then
            assertNull(store.load("missing"))
        }
}
