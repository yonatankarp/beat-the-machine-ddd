package com.yonatankarp.beatthemachine.application

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.service.ForfeitChallengeService
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ForfeitChallengeServiceTest {
    private val store = linkedMapOf<ChallengeId, Challenge>()
    private val repo =
        object : ChallengeRepository {
            override fun save(challenge: Challenge) = challenge.also { store[it.id] = it }

            override fun findById(id: ChallengeId) = store[id]
        }

    private fun seed(): Challenge = Challenge.start(Prompt("hello world"), Lives(3)).also { store[it.id] = it }

    @Test
    fun `forfeit loads the challenge, sets LOST, and persists it`() {
        val c = seed()
        val service = ForfeitChallengeService(repo)
        val result = service.forfeit(c.id)
        assertEquals(ChallengeStatus.LOST, result.status)
        assertTrue(store.containsKey(c.id))
        assertEquals(ChallengeStatus.LOST, store[c.id]?.status)
    }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() {
        val service = ForfeitChallengeService(repo)
        assertFailsWith<ChallengeNotFound> {
            service.forfeit(ChallengeId.new())
        }
    }
}
