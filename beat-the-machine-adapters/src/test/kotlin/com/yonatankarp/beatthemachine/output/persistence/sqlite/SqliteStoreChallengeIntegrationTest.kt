package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SqliteStoreChallengeIntegrationTest {
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
            val challenge = mediumChallenge(prompt = "pixel art cat".asPrompt())

            // When
            val saved = storeChallenge handle StoreChallenge.Command(challenge)

            // Then
            assertEquals(1L, saved.version)
        }

    @Test
    fun `allows sequential stores with updated versions`() =
        runTest {
            // Given
            val challenge = mediumChallenge(prompt = "sequential".asPrompt())

            // When
            val v1 = storeChallenge handle StoreChallenge.Command(challenge)
            val v2 = storeChallenge handle StoreChallenge.Command(v1)

            // Then
            assertEquals(1L, v1.version)
            assertEquals(2L, v2.version)
        }

    @Test
    fun `rejects a stale version on second store`() =
        runTest {
            // Given
            val c = mediumChallenge()
            storeChallenge handle StoreChallenge.Command(c)

            // When / Then
            assertFailsWith<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(c) }
        }

    @Test
    fun `persists all difficulty levels`() =
        runTest {
            // Given
            val difficulties = Difficulty.entries

            // When / Then
            difficulties.forEach { diff ->
                val c = Challenge.start("test prompt".asPrompt(), 2.lives(), difficulty = diff)
                storeChallenge handle StoreChallenge.Command(c)
                val found = findChallengeById answer FindChallengeById.Query(c.id)
                assertNotNull(found)
                assertEquals(diff, found.difficulty)
            }
        }

    @Test
    fun `persists picture states correctly`() =
        runTest {
            // Given
            val pending = mediumChallenge(prompt = "pending pic".asPrompt())
            val ready = mediumChallenge(prompt = "ready pic".asPrompt(), picture = readyPicture())
            val failed = mediumChallenge(prompt = "failed pic".asPrompt(), picture = failedPicture())

            // When
            storeChallenge handle StoreChallenge.Command(pending)
            storeChallenge handle StoreChallenge.Command(ready)
            storeChallenge handle StoreChallenge.Command(failed)

            // Then
            assertEquals(Picture.Pending, (findChallengeById answer FindChallengeById.Query(pending.id))?.picture)
            assertEquals(
                Picture.Ready("https://example.com/img.png"),
                (findChallengeById answer FindChallengeById.Query(ready.id))?.picture,
            )
            assertEquals(Picture.Failed, (findChallengeById answer FindChallengeById.Query(failed.id))?.picture)
        }
}
