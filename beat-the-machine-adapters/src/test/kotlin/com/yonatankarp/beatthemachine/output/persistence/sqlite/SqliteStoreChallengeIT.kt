package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SqliteStoreChallengeIT {
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
    fun `stores a fresh challenge and bumps the version`() =
        runTest {
            // Given
            val challenge = Challenge.start(Prompt("pixel art cat"), Lives(5))

            // When
            val saved = storeChallenge(challenge)

            // Then
            assertEquals(1L, saved.version)
        }

    @Test
    fun `allows sequential stores with updated versions`() =
        runTest {
            // Given
            val challenge = Challenge.start(Prompt("sequential"), Lives(3))

            // When
            val v1 = storeChallenge(challenge)
            val v2 = storeChallenge(v1)

            // Then
            assertEquals(1L, v1.version)
            assertEquals(2L, v2.version)
        }

    @Test
    fun `rejects a stale version on second store`() =
        runTest {
            // Given
            val c = Challenge.start(Prompt("hello world"), Lives(3))
            storeChallenge(c)

            // When / Then
            assertFailsWith<OptimisticLockConflict> { storeChallenge(c) }
        }

    @Test
    fun `persists all difficulty levels`() =
        runTest {
            // Given
            val difficulties = Difficulty.entries

            // When / Then
            difficulties.forEach { diff ->
                val c = Challenge.start(Prompt("test prompt"), Lives(2), difficulty = diff)
                storeChallenge(c)
                val found = findChallengeById(c.id)
                assertNotNull(found)
                assertEquals(diff, found.difficulty)
            }
        }

    @Test
    fun `persists picture states correctly`() =
        runTest {
            // Given
            val pending = Challenge.start(Prompt("pending pic"), Lives(2), picture = Picture.Pending)
            val ready = Challenge.start(Prompt("ready pic"), Lives(2), picture = Picture.Ready("https://example.com/img.png"))
            val failed = Challenge.start(Prompt("failed pic"), Lives(2), picture = Picture.Failed)

            // When
            storeChallenge(pending)
            storeChallenge(ready)
            storeChallenge(failed)

            // Then
            assertEquals(Picture.Pending, findChallengeById(pending.id)?.picture)
            assertEquals(Picture.Ready("https://example.com/img.png"), findChallengeById(ready.id)?.picture)
            assertEquals(Picture.Failed, findChallengeById(failed.id)?.picture)
        }
}
