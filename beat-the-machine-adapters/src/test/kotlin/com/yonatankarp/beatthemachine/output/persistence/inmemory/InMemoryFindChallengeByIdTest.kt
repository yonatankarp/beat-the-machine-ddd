package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
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
            val saved = storeChallenge(Challenge.start(Prompt("hello world"), Lives(3)))

            // When
            val found = findChallengeById(saved.id)

            // Then
            assertEquals(saved.id, found?.id)
        }

    @Test
    fun `returns null for an unknown id`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val findChallengeById = InMemoryFindChallengeById(store)
            val unknownId = ChallengeId.new()

            // When
            val found = findChallengeById(unknownId)

            // Then
            assertNull(found)
        }
}
