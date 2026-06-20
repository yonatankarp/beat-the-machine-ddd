package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryFindChallengeByIdTest {
    @Test
    fun `finds a stored challenge`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findChallengeById = InMemoryFindChallengeById(store)
            val saved = storeChallenge handle StoreChallenge.Command(mediumChallenge())

            // When
            val found = findChallengeById answer FindChallengeById.Query(saved.id)

            // Then
            assertEquals(saved.id, found?.id)
        }

    @Test
    fun `returns null for an unknown id`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val findChallengeById = InMemoryFindChallengeById(store)
            val unknownId = aChallengeId()

            // When
            val found = findChallengeById answer FindChallengeById.Query(unknownId)

            // Then
            assertNull(found)
        }
}
