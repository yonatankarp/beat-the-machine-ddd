package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val InMemoryStoreChallengesSuite by testSuite {
    given("a new challenge") {
        whenever("storing it") {
            then("it bumps the version") {
                val store = InMemoryChallengeStore()
                val storeChallenge = InMemoryStoreChallenge(store)
                val challenge = mediumChallenge()
                val saved = storeChallenge handle StoreChallenge.Command(challenge)
                saved.version shouldBe 1L
            }
        }
    }

    given("an already stored challenge") {
        whenever("storing it again with a stale version") {
            then("it throws OptimisticLockConflict") {
                val store = InMemoryChallengeStore()
                val storeChallenge = InMemoryStoreChallenge(store)
                val c = mediumChallenge()
                storeChallenge handle StoreChallenge.Command(c)
                shouldThrow<OptimisticLockConflict> { storeChallenge handle StoreChallenge.Command(c) }
            }
        }
    }
}
