package com.yonatankarp.beatthemachine.application

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.service.MakeGuessService
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Guess
import com.yonatankarp.beatthemachine.domain.GuessOutcome
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.MaskedToken
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MakeGuessServiceTest {
    private val store = linkedMapOf<ChallengeId, Challenge>()
    private val repo =
        object : ChallengeRepository {
            override fun save(challenge: Challenge) = challenge.also { store[it.id] = it }

            override fun findById(id: ChallengeId) = store[id]
        }

    private fun seed(): Challenge = Challenge.start(Prompt("hello world"), Lives(3)).also { store[it.id] = it }

    @Test
    fun `a hit is persisted`() {
        val c = seed()
        val service = MakeGuessService(repo)
        val (updated, outcome) = service.guess(c.id, Guess("hello"))
        assertEquals(GuessOutcome.HIT, outcome)
        assertEquals(MaskedToken.Revealed("hello"), updated.maskedPrompt().tokens[0])
        assertEquals(MaskedToken.Revealed("hello"), store[c.id]?.maskedPrompt()?.tokens?.get(0))
    }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() {
        val service = MakeGuessService(repo)
        assertFailsWith<ChallengeNotFound> {
            service.guess(ChallengeId.new(), Guess("hello"))
        }
    }
}
