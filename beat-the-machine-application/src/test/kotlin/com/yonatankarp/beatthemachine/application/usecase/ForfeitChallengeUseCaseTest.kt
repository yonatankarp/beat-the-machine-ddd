package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val ForfeitChallengeUseSuite by testSuite {
    given("a stored challenge") {
        val store by setup { FakeChallengeStore() }
        val challenge by setup { store handle StoreChallenge.Command(mediumChallenge()) }
        val forfeitChallenge by setup { ForfeitChallengeUseCase(store, store) }

        whenever("forfeiting it") {
            val result by action { forfeitChallenge handle ForfeitChallenge.Command(challenge.id) }

            then("it sets the status to LOST and persists it") {
                result.status shouldBe ChallengeStatus.LOST
                (store answer FindChallengeById.Query(challenge.id))?.status shouldBe ChallengeStatus.LOST
            }
        }
    }

    given("an unknown challenge") {
        val store by setup { FakeChallengeStore() }
        val forfeitChallenge by setup { ForfeitChallengeUseCase(store, store) }
        val unknownId by setup { aChallengeId() }

        whenever("forfeiting it") {
            val result by action {
                shouldThrow<ChallengeNotFound> {
                    forfeitChallenge handle ForfeitChallenge.Command(unknownId)
                }
            }

            then("it throws ChallengeNotFound") {
                result.id shouldBe unknownId
            }
        }
    }
}
