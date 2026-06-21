package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val InMemoryFindChallengeByIdSuite by testSuite {
    test("finds a stored challenge") {
        // Given
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val findChallengeById = InMemoryFindChallengeById(store)
        val saved = storeChallenge(mediumChallenge())

        // When
        val found = findChallengeById(saved.id)

        // Then
        found?.id shouldBe saved.id
    }

    test("returns null for an unknown id") {
        // Given
        val store = InMemoryChallengeStore()
        val findChallengeById = InMemoryFindChallengeById(store)
        val unknownId = aChallengeId()

        // When
        val found = findChallengeById(unknownId)

        // Then
        found.shouldBeNull()
    }
}
