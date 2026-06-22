package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val GetChallengeSuite by testSuite {
    given("a stored challenge") {
        val store by setup { FakeChallengeStore() }
        val getChallenge by setup { GetChallengeUseCase(store) }
        val challenge by setup { store handle StoreChallenge.Command(mediumChallenge(prompt = "red fox".asPrompt())) }

        whenever("getting it by id") {
            val result by action { getChallenge answer GetChallenge.Query(challenge.id) }

            then("it returns the challenge") {
                result shouldBe challenge
            }
        }
    }

    given("an unknown id") {
        val store by setup { FakeChallengeStore() }
        val getChallenge by setup { GetChallengeUseCase(store) }
        val unknownId by setup { aChallengeId() }

        whenever("getting the challenge") {
            val result by action {
                shouldThrow<ChallengeNotFound> { getChallenge answer GetChallenge.Query(unknownId) }
            }

            then("it throws ChallengeNotFound") {
                result.id shouldBe unknownId
            }
        }
    }
}
