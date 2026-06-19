package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartChallengeUseCaseTest {
    private val fakePrompt = Prompt("hello world")
    private val prompts = PromptSource { fakePrompt }
    private val store = FakeChallengeStore()

    @Test
    fun `starts a pending challenge and enqueues picture generation`() =
        runTest {
            val enqueued = mutableListOf<ChallengeId>()
            val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }
            val challenge = startChallenge(Difficulty.MEDIUM)
            assertEquals(Picture.Pending, challenge.picture)
            assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
            assertEquals(listOf(challenge.id), enqueued)
            assertTrue(store.byId.containsKey(challenge.id))
        }

    @Test
    fun `starting lives scale with difficulty`() =
        runTest {
            val startChallenge = StartChallengeUseCase(prompts, store) {}
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.EASY).remaining, startChallenge(Difficulty.EASY).lives.remaining)
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.MEDIUM).remaining, startChallenge(Difficulty.MEDIUM).lives.remaining)
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.HARD).remaining, startChallenge(Difficulty.HARD).lives.remaining)
        }
}
