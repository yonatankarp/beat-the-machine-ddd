package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val GetChallengeSuite by testSuite {
    test("returns a challenge that exists") {
        val store = FakeChallengeStore()
        val getChallenge = GetChallengeUseCase(store)
        val challenge = store handle StoreChallenge.Command(mediumChallenge(prompt = "red fox".asPrompt()))
        val result = getChallenge answer GetChallenge.Query(challenge.id)
        result shouldBe challenge
    }

    test("throws ChallengeNotFound for an unknown id") {
        val store = FakeChallengeStore()
        val getChallenge = GetChallengeUseCase(store)
        val unknownId = aChallengeId()
        shouldThrow<ChallengeNotFound> { getChallenge answer GetChallenge.Query(unknownId) }
    }
}
