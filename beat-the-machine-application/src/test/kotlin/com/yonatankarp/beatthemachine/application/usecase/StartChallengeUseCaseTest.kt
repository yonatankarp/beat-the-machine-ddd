package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartChallengeUseCaseTest {
    private val prompts = PromptSource { Prompt("hello world") }
    private val store = FakeChallengeStore()

    @Test
    fun `starts a pending challenge and enqueues picture generation`() {
        val enqueued = mutableListOf<ChallengeId>()
        val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }
        val challenge = startChallenge(Difficulty.MEDIUM)
        assertEquals(Picture.Pending, challenge.picture)
        assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
        assertEquals(listOf(challenge.id), enqueued)
        assertTrue(store.byId.containsKey(challenge.id))
    }

    @Test
    fun `starting lives scale with difficulty`() {
        val startChallenge = StartChallengeUseCase(prompts, store) {}
        assertEquals(8, startChallenge(Difficulty.EASY).lives.remaining)
        assertEquals(6, startChallenge(Difficulty.MEDIUM).lives.remaining)
        assertEquals(4, startChallenge(Difficulty.HARD).lives.remaining)
    }
}
