package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val GetChallengeUseCaseSuite by testSuite {
    test("returns a challenge that exists") {
        // Given
        val store = FakeChallengeStore() // mutable: fresh per test
        val getChallenge = GetChallengeUseCase(store)
        val challenge = store(mediumChallenge(prompt = "red fox".asPrompt()))

        // When
        val result = getChallenge(challenge.id)

        // Then
        result shouldBe challenge
    }

    test("throws ChallengeNotFound for an unknown id") {
        // Given
        val store = FakeChallengeStore()
        val getChallenge = GetChallengeUseCase(store)
        val unknownId = aChallengeId()

        // When / Then
        shouldThrow<ChallengeNotFound> { getChallenge(unknownId) }
    }
}
