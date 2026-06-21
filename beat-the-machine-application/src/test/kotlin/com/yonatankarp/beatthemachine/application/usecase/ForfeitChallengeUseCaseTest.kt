package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val ForfeitChallengeUseSuite by testSuite {
    test("forfeit loads the challenge, sets LOST, and persists it") {
        val store = FakeChallengeStore()
        val c = store handle StoreChallenge.Command(mediumChallenge())
        val forfeitChallenge = ForfeitChallengeUseCase(store, store)
        val result = forfeitChallenge handle ForfeitChallenge.Command(c.id)
        result.status shouldBe ChallengeStatus.LOST
        (store answer FindChallengeById.Query(c.id))?.status shouldBe ChallengeStatus.LOST
    }

    test("an unknown challenge throws ChallengeNotFound") {
        val store = FakeChallengeStore()
        val forfeitChallenge = ForfeitChallengeUseCase(store, store)
        val unknownId = aChallengeId()
        shouldThrow<ChallengeNotFound> {
            forfeitChallenge handle ForfeitChallenge.Command(unknownId)
        }
    }
}
