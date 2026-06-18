package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MakeGuessUseCaseTest {
    private val store = FakeChallengeStore()

    private fun seed(): Challenge = store(Challenge.start(Prompt("hello world"), Lives(3)))

    @Test
    fun `a hit is persisted`() {
        val c = seed()
        val makeGuess = MakeGuessUseCase(store, store)
        val (updated, outcome) = makeGuess(c.id, Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(MaskedToken.Revealed("hello"), updated.maskedPrompt().tokens[0])
        assertEquals(MaskedToken.Revealed("hello"), store(c.id)?.maskedPrompt()?.tokens?.get(0))
    }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() {
        val makeGuess = MakeGuessUseCase(store, store)
        assertFailsWith<ChallengeNotFound> {
            makeGuess(ChallengeId.new(), Guess("hello"))
        }
    }

    @Test
    fun `an optimistic-lock conflict on store propagates`() {
        val c = seed()
        val conflicting = StoreChallenge { throw OptimisticLockConflict(it.id) }
        assertFailsWith<OptimisticLockConflict> {
            MakeGuessUseCase(store, conflicting)(c.id, Guess("hello"))
        }
    }
}
