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
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val MakeGuessUseSuite by testSuite {
    given("a stored challenge") {
        whenever("a correct guess is made") {
            then("the hit is returned and persisted") {
                val store = FakeChallengeStore()
                val c = store handle StoreChallenge.Command(mediumChallenge())
                val makeGuess = MakeGuessUseCase(store, store)
                val guess = "hello".asGuess()
                val (updated, outcome) = makeGuess handle MakeGuess.Command(c.id, guess)
                outcome shouldBe GuessOutcome.HIT
                updated.maskedPrompt().tokens[0] shouldBe MaskedToken.Revealed("hello")
                (store answer FindChallengeById.Query(c.id))?.maskedPrompt()?.tokens?.get(0) shouldBe MaskedToken.Revealed("hello")
            }
        }
    }

    given("an unknown challenge") {
        whenever("a guess is made") {
            then("it throws ChallengeNotFound") {
                val store = FakeChallengeStore()
                val makeGuess = MakeGuessUseCase(store, store)
                val unknownId = aChallengeId()
                val guess = "hello".asGuess()
                shouldThrow<ChallengeNotFound> {
                    makeGuess handle MakeGuess.Command(unknownId, guess)
                }
            }
        }
    }

    given("a store that conflicts on write") {
        whenever("a guess is made") {
            then("the optimistic-lock conflict propagates") {
                val store = FakeChallengeStore()
                val c = store handle StoreChallenge.Command(mediumChallenge())
                val conflicting =
                    object : StoreChallenge {
                        override suspend fun handle(command: StoreChallenge.Command): Challenge =
                            throw OptimisticLockConflict(command.challenge.id)
                    }
                val makeGuess = MakeGuessUseCase(store, conflicting)
                val guess = "hello".asGuess()
                shouldThrow<OptimisticLockConflict> {
                    makeGuess handle MakeGuess.Command(c.id, guess)
                }
            }
        }
    }
}
