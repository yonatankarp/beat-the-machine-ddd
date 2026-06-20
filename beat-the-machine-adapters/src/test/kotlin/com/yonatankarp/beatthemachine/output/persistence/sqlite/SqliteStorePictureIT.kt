package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqliteStorePictureIT {
    private val jdbc = newSqliteJdbc()
    private val storePicture = SqliteStorePicture(jdbc)

    @Test
    fun `handle returns an id and writes to picture table`() =
        runTest {
            // Given
            val bytes = byteArrayOf(1, 2, 3, 4)

            // When
            val id = storePicture handle StorePicture.Command(bytes, "image/png")

            // Then
            assertTrue(id.isNotBlank())
            val pictureRows = jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
            assertEquals(1, pictureRows)
        }

    @Test
    fun `bytes live in the picture table, not on challenge`() =
        runTest {
            // Given / When
            storePicture handle StorePicture.Command(byteArrayOf(1), "image/png")

            // Then
            val pictureRows = jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
            val challengeRows = jdbc.queryForObject("SELECT COUNT(*) FROM challenge", Int::class.java)
            assertEquals(1, pictureRows)
            assertEquals(0, challengeRows)
        }
}
