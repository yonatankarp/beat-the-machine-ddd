package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val InMemoryStoreChallengeTestSuite by testSuite {
    test("stores and bumps the version") {
        // Given
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val challenge = mediumChallenge()

        // When
        val saved = storeChallenge(challenge)

        // Then
        saved.version shouldBe 1L
    }

    test("rejects a stale version") {
        // Given
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val c = mediumChallenge()
        storeChallenge(c)

        // When / Then
        shouldThrow<OptimisticLockConflict> { storeChallenge(c) }
    }
}
