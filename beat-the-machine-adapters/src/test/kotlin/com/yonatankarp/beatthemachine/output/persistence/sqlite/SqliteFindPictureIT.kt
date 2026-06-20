package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqliteFindPictureIT {
    private val jdbc = newSqliteJdbc()
    private val storePicture = SqliteStorePicture(jdbc)
    private val findPicture = SqliteFindPicture(jdbc)

    @Test
    fun `round-trips the bytes and content type`() =
        runTest {
            // Given
            val bytes = byteArrayOf(1, 2, 3, 4)

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
            assertNull(findPicture answer FindPicture.Query("missing"))
        }
}
