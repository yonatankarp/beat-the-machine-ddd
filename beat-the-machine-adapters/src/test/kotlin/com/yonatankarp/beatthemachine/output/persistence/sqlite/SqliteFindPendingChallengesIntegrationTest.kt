package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val SqliteFindPendingChallengesITSuite by testSuite {
    test("returns only challenges whose picture is pending") {
        val (storeChallenge, _, findPendingChallenges) = newSqliteAdapters()
        val pendingA = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "pending one".asPrompt()))
        val pendingB = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "pending two".asPrompt()))
        storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "ready pic".asPrompt(), picture = readyPicture()))
        storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "failed pic".asPrompt(), picture = failedPicture()))
        val ids = (findPendingChallenges answer FindPendingChallenges.Query).map { it.id }.toSet()
        ids shouldBe setOf(pendingA.id, pendingB.id)
    }
}
