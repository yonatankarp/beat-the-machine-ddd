package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

val MakeGuessUseCaseSuite by testSuite {
    test("a hit is persisted") {
        // Given
        val store = FakeChallengeStore() // mutable: fresh per test
        val c: Challenge = store(mediumChallenge())
        val makeGuess = MakeGuessUseCase(store, store)
        val guess = "hello".asGuess()

        // When
        val (updated, outcome) = makeGuess(c.id, guess)

        // Then
        outcome shouldBe GuessOutcome.HIT
        updated.maskedPrompt().tokens[0] shouldBe MaskedToken.Revealed("hello")
        store(c.id)?.maskedPrompt()?.tokens?.get(0) shouldBe MaskedToken.Revealed("hello")
    }

    test("an unknown challenge throws ChallengeNotFound") {
        // Given
        val store = FakeChallengeStore()
        val makeGuess = MakeGuessUseCase(store, store)
        val unknownId = aChallengeId()
        val guess = "hello".asGuess()

        // When / Then
        shouldThrow<ChallengeNotFound> {
            makeGuess(unknownId, guess)
        }
    }

    test("an optimistic-lock conflict on store propagates") {
        // Given
        val store = FakeChallengeStore()
        val c: Challenge = store(mediumChallenge())
        val conflicting = StoreChallenge { throw OptimisticLockConflict(it.id) }
        val makeGuess = MakeGuessUseCase(store, conflicting)
        val guess = "hello".asGuess()

        // When / Then
        shouldThrow<OptimisticLockConflict> {
            makeGuess(c.id, guess)
        }
    }
}
