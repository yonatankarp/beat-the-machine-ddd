package com.yonatankarp.beatthemachine.application

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.service.GetChallengeService
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetChallengeServiceTest {
    private val stored = mutableMapOf<ChallengeId, Challenge>()
    private val repo =
        object : ChallengeRepository {
            override fun save(challenge: Challenge) = challenge.also { stored[it.id] = it }

            override fun findById(id: ChallengeId) = stored[id]
        }
    private val service = GetChallengeService(repo)

    @Test
    fun `returns a challenge that exists`() {
        val challenge = Challenge.start(Prompt("red fox"), Lives(6))
        stored[challenge.id] = challenge

        val result = service.get(challenge.id)

        assertEquals(challenge, result)
    }

    @Test
    fun `throws ChallengeNotFound for an unknown id`() {
        val unknownId = ChallengeId.new()

        assertFailsWith<ChallengeNotFound> { service.get(unknownId) }
    }
}
