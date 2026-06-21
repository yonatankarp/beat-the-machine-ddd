package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val InMemoryStoreChallengesSuite by testSuite {
    test("stores and bumps the version") {
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val challenge = mediumChallenge()
        val saved = storeChallenge handle StoreChallenge.Command(challenge)
        saved.version shouldBe 1L
    }

    test("rejects a stale version") {
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val c = mediumChallenge()
        storeChallenge handle StoreChallenge.Command(c)
        shouldThrow<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(c) }
    }
}
