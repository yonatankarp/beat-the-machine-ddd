package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val SqliteFindChallengeByIdITSuite by testSuite {
    test("finds a stored challenge with its fields intact") {
        val (storeChallenge, findChallengeById, _) = newSqliteAdapters()
        val c = mediumChallenge(lives = 5.lives(), prompt = "pixel art cat".asPrompt())
        storeChallenge(c)

        val found = findChallengeById(c.id)

        found.shouldNotBeNull()
        found.id shouldBe c.id
        found.secretPrompt().text shouldBe "pixel art cat"
        found.lives.remaining shouldBe 5
    }

    test("returns null for an unknown id") {
        val (_, findChallengeById, _) = newSqliteAdapters()
        val unknownId = aChallengeId()

        val found = findChallengeById(unknownId)

        found.shouldBeNull()
    }
}
