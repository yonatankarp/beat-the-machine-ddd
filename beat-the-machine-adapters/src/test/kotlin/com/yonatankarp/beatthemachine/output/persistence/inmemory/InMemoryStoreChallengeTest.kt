package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InMemoryStoreChallengeTest {
    @Test
    fun `stores and bumps the version`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val challenge = mediumChallenge(lives = 3.lives())

            // When
            val saved = storeChallenge(challenge)

            // Then
            assertEquals(1L, saved.version)
        }

    @Test
    fun `rejects a stale version`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val c = mediumChallenge(lives = 3.lives())
            storeChallenge(c)

            // When / Then
            assertFailsWith<OptimisticLockConflict> { storeChallenge(c) }
        }
}
