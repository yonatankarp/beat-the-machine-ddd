package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val InMemoryFindPendingChallengesSuite by testSuite {
    given("challenges with pending and ready pictures") {
        whenever("finding pending challenges") {
            then("it returns only the ones whose picture is pending") {
                val store = InMemoryChallengeStore()
                val storeChallenge = InMemoryStoreChallenge(store)
                val findPendingChallenges = InMemoryFindPendingChallenges(store)
                val pendingA = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                val pendingB = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "red fox".asPrompt()))
                storeChallenge handle
                    StoreChallenge.Command(mediumChallenge(prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/1.png")))
                val ids = (findPendingChallenges answer FindPendingChallenges.Query).map { it.id }.toSet()
                ids shouldBe setOf(pendingA.id, pendingB.id)
            }
        }
    }
}
