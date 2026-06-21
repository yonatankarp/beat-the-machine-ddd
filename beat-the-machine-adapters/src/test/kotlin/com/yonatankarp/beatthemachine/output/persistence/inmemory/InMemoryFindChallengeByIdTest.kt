package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val InMemoryFindChallengeByIdSuite by testSuite {
    test("finds a stored challenge") {
        val store = InMemoryChallengeStore()
        val storeChallenge = InMemoryStoreChallenge(store)
        val findChallengeById = InMemoryFindChallengeById(store)
        val saved = storeChallenge handle StoreChallenge.Command(mediumChallenge())
        val found = findChallengeById answer FindChallengeById.Query(saved.id)
        found?.id shouldBe saved.id
    }

    test("returns null for an unknown id") {
        val store = InMemoryChallengeStore()
        val findChallengeById = InMemoryFindChallengeById(store)
        val unknownId = aChallengeId()
        val found = findChallengeById answer FindChallengeById.Query(unknownId)
        found.shouldBeNull()
    }
}
