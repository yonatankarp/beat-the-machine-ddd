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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MakeGuessUseCaseTest {
    private val store = FakeChallengeStore()

    private suspend fun seed(): Challenge = store(mediumChallenge())

    @Test
    fun `a hit is persisted`() =
        runTest {
            // Given
            val c = seed()
            val makeGuess = MakeGuessUseCase(store, store)
            val guess = "hello".asGuess()

            // When
            val (updated, outcome) = makeGuess handle MakeGuess.Command(c.id, guess)

            // Then
            assertEquals(GuessOutcome.HIT, outcome)
            assertEquals(MaskedToken.Revealed("hello"), updated.maskedPrompt().tokens[0])
            assertEquals(MaskedToken.Revealed("hello"), (store answer FindChallengeById.Query(c.id))?.maskedPrompt()?.tokens?.get(0))
        }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() =
        runTest {
            // Given
            val makeGuess = MakeGuessUseCase(store, store)
            val unknownId = aChallengeId()
            val guess = "hello".asGuess()

            // When / Then
            assertFailsWith<ChallengeNotFound> {
                makeGuess handle MakeGuess.Command(unknownId, guess)
            }
        }

    @Test
    fun `an optimistic-lock conflict on store propagates`() =
        runTest {
            // Given
            val c = seed()
            val conflicting = StoreChallenge { throw OptimisticLockConflict(it.id) }
            val makeGuess = MakeGuessUseCase(store, conflicting)
            val guess = "hello".asGuess()

            // When / Then
            assertFailsWith<OptimisticLockConflict> {
                makeGuess handle MakeGuess.Command(c.id, guess)
            }
        }
}
