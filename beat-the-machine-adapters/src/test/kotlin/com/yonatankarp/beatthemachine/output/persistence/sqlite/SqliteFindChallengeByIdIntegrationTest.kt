package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val SqliteFindChallengeByIdITSuite by testSuite {
    given("a stored challenge") {
        whenever("finding it by id") {
            then("it finds the stored challenge with its fields intact") {
                val (storeChallenge, findChallengeById) = newSqliteAdapters()
                val c = mediumChallenge(lives = 5.lives(), prompt = "pixel art cat".asPrompt())
                storeChallenge handle StoreChallenge.Command(c)
                val found = findChallengeById answer FindChallengeById.Query(c.id)
                found.shouldNotBeNull()
                found.id shouldBe c.id
                found.secretPrompt().text shouldBe "pixel art cat"
                found.lives.remaining shouldBe 5
            }
        }
    }

    given("an unknown id") {
        whenever("finding the challenge by id") {
            then("it returns null") {
                val (_, findChallengeById) = newSqliteAdapters()
                val unknownId = aChallengeId()
                val found = findChallengeById answer FindChallengeById.Query(unknownId)
                found.shouldBeNull()
            }
        }
    }
}
