package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val InMemoryFindChallengeByIdSuite by testSuite {
    given("a stored challenge") {
        whenever("finding it by id") {
            then("it returns the stored challenge") {
                val store = InMemoryChallengeStore()
                val storeChallenge = InMemoryStoreChallenge(store)
                val findChallengeById = InMemoryFindChallengeById(store)
                val saved = storeChallenge handle StoreChallenge.Command(mediumChallenge())
                val found = findChallengeById answer FindChallengeById.Query(saved.id)
                found?.id shouldBe saved.id
            }
        }
    }

    given("an unknown id") {
        whenever("finding the challenge") {
            then("it returns null") {
                val store = InMemoryChallengeStore()
                val findChallengeById = InMemoryFindChallengeById(store)
                val unknownId = aChallengeId()
                val found = findChallengeById answer FindChallengeById.Query(unknownId)
                found.shouldBeNull()
            }
        }
    }
}
