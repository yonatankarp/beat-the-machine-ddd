package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SqliteFindChallengeByIdIT {
    private lateinit var storeChallenge: SqliteStoreChallenge
    private lateinit var findChallengeById: SqliteFindChallengeById

    @BeforeEach
    fun setup() {
        val jdbc = newSqliteJdbc()
        val mapper = ChallengeRowMapper()
        storeChallenge = SqliteStoreChallenge(jdbc, mapper)
        findChallengeById = SqliteFindChallengeById(jdbc, mapper)
    }

    @Test
    fun `finds a stored challenge with its fields intact`() =
        runTest {
            // Given
            val c = mediumChallenge(lives = 5.lives(), prompt = "pixel art cat".asPrompt())
            storeChallenge(c)

            // When
            val found = findChallengeById(c.id)

            // Then
            assertNotNull(found)
            assertEquals(c.id, found.id)
            assertEquals("pixel art cat", found.secretPrompt().text)
            assertEquals(5, found.lives.remaining)
        }

    @Test
    fun `returns null for an unknown id`() =
        runTest {
            // Given
            val unknownId = aChallengeId()

            // When
            val found = findChallengeById(unknownId)

            // Then
            assertNull(found)
        }
}
