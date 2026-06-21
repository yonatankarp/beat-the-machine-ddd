package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val ForfeitChallengeUseCaseSuite by testSuite {
    test("forfeit loads the challenge, sets LOST, and persists it") {
        // Given
        val store = FakeChallengeStore() // mutable: fresh per test
        val c: Challenge = store(mediumChallenge())
        val forfeitChallenge = ForfeitChallengeUseCase(store, store)

        // When
        val result = forfeitChallenge(c.id)

        // Then
        result.status shouldBe ChallengeStatus.LOST
        store(c.id)?.status shouldBe ChallengeStatus.LOST
    }

    test("an unknown challenge throws ChallengeNotFound") {
        // Given
        val store = FakeChallengeStore()
        val forfeitChallenge = ForfeitChallengeUseCase(store, store)
        val unknownId = aChallengeId()

        // When / Then
        shouldThrow<ChallengeNotFound> {
            forfeitChallenge(unknownId)
        }
    }
}
