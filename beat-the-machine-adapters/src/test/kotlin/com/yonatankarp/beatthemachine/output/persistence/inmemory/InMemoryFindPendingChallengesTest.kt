package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val InMemoryFindPendingChallengesSuite by testSuite {
    test("returns only challenges whose picture is pending") {
        // Given
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val findPendingChallenges = InMemoryFindPendingChallenges(store)
        val pendingA = storeChallenge(mediumChallenge())
        val pendingB = storeChallenge(mediumChallenge(prompt = "red fox".asPrompt()))
        storeChallenge(mediumChallenge(prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/1.png")))

        // When
        val ids = findPendingChallenges().map { it.id }.toSet()

        // Then
        ids shouldBe setOf(pendingA.id, pendingB.id)
    }
}
