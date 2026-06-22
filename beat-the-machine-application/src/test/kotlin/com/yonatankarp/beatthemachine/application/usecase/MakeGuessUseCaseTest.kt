package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val MakeGuessUseSuite by testSuite {
    given("a stored challenge") {
        val store by setup { FakeChallengeStore() }
        val challenge by setup { store handle StoreChallenge.Command(mediumChallenge()) }
        val makeGuess by setup { MakeGuessUseCase(store, store) }
        val guess by setup { "hello".asGuess() }

        whenever("a correct guess is made") {
            val result by action { makeGuess handle MakeGuess.Command(challenge.id, guess) }

            then("the hit is returned and persisted") {
                val (updated, outcome) = result
                outcome shouldBe GuessOutcome.HIT
                updated.maskedPrompt().tokens[0] shouldBe MaskedToken.Revealed("hello")
                (store answer FindChallengeById.Query(challenge.id))?.maskedPrompt()?.tokens?.get(0) shouldBe MaskedToken.Revealed("hello")
            }
        }
    }

    given("an unknown challenge") {
        val store by setup { FakeChallengeStore() }
        val makeGuess by setup { MakeGuessUseCase(store, store) }
        val unknownId by setup { aChallengeId() }
        val guess by setup { "hello".asGuess() }

        whenever("a guess is made") {
            val result by action {
                shouldThrow<ChallengeNotFound> {
                    makeGuess handle MakeGuess.Command(unknownId, guess)
                }
            }

            then("it throws ChallengeNotFound") {
                result.id shouldBe unknownId
            }
        }
    }

    given("a store that conflicts on write") {
        val store by setup { FakeChallengeStore() }
        val challenge by setup { store handle StoreChallenge.Command(mediumChallenge()) }
        val conflicting by setup {
            object : StoreChallenge {
                override suspend fun handle(command: StoreChallenge.Command): Challenge = throw OptimisticLockConflict(command.challenge.id)
            }
        }
        val makeGuess by setup { MakeGuessUseCase(store, conflicting) }
        val guess by setup { "hello".asGuess() }

        whenever("a guess is made") {
            val result by action {
                shouldThrow<OptimisticLockConflict> {
                    makeGuess handle MakeGuess.Command(challenge.id, guess)
                }
            }

            then("the optimistic-lock conflict propagates") {
                result.id shouldBe challenge.id
            }
        }
    }
}
