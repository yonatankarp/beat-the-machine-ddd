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
            val url = store.save(byteArrayOf(1, 2, 3, 4), "image/png")
            val id = url.removePrefix("/images/")
            val loaded = store.load(id)!!
            assertEquals("image/png", loaded.contentType)
            assertTrue(byteArrayOf(1, 2, 3, 4).contentEquals(loaded.bytes))
        }

    @Test
    fun `bytes live in the picture table, not on challenge`() =
        runTest {
            store.save(byteArrayOf(1), "image/png")
            val pictureRows = jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
            val challengeRows = jdbc.queryForObject("SELECT COUNT(*) FROM challenge", Int::class.java)
            assertEquals(1, pictureRows)
            assertEquals(0, challengeRows)
        }

    @Test
    fun `load returns null for unknown id`() =
        runTest {
            assertNull(store.load("missing"))
        }
}
