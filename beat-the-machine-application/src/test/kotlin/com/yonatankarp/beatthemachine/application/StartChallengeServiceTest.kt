package com.yonatankarp.beatthemachine.application

import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.port.out.PromptSource
import com.yonatankarp.beatthemachine.application.service.StartChallengeService
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Picture
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartChallengeServiceTest {
    private val prompts =
        object : PromptSource {
            override fun next(difficulty: Difficulty) = Prompt("hello world")
        }
    private val saved = mutableListOf<Challenge>()
    private val repo =
        object : ChallengeRepository {
            override fun save(challenge: Challenge) = challenge.also { saved.add(it) }

            override fun findById(id: ChallengeId) = saved.firstOrNull { it.id == id }
        }

    @Test
    fun `starts a pending challenge and enqueues picture generation`() {
        val enqueued = mutableListOf<ChallengeId>()
        val service = StartChallengeService(prompts, repo) { enqueued.add(it) }
        val challenge = service.start(Difficulty.MEDIUM)
        assertEquals(Picture.Pending, challenge.picture)
        assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
        assertEquals(listOf(challenge.id), enqueued)
        assertTrue(saved.contains(challenge))
    }
}
