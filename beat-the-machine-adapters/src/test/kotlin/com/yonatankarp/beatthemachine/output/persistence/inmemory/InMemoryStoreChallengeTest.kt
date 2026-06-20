package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
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
            val challenge = mediumChallenge()

            // When
            val saved = storeChallenge handle StoreChallenge.Command(challenge)

            // Then
            assertEquals(1L, saved.version)
        }

    @Test
    fun `rejects a stale version`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val c = mediumChallenge()
            storeChallenge handle StoreChallenge.Command(c)

            // When / Then
            assertFailsWith<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(c) }
        }
}
