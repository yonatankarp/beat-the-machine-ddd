package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartChallengeUseCaseTest {
    private val prompts = PromptSource { Prompt("hello world") }
    private val store = FakeChallengeStore()

    @Test
    fun `starts a pending challenge and enqueues picture generation`() =
        runTest {
            // Given
            val enqueued = mutableListOf<ChallengeId>()
            val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }

            // When
            val challenge = startChallenge(Difficulty.MEDIUM)

            // Then
            assertEquals(Picture.Pending, challenge.picture)
            assertEquals(ChallengeStatus.IN_PROGRESS, challenge.status)
            assertEquals(listOf(challenge.id), enqueued)
            assertTrue(store.byId.containsKey(challenge.id))
        }

    @Test
    fun `starting lives scale with difficulty`() =
        runTest {
            // Given
            val startChallenge = StartChallengeUseCase(prompts, store) {}

            // When
            val easy = startChallenge(Difficulty.EASY)
            val medium = startChallenge(Difficulty.MEDIUM)
            val hard = startChallenge(Difficulty.HARD)

            // Then
            assertEquals(8, easy.lives.remaining)
            assertEquals(6, medium.lives.remaining)
            assertEquals(4, hard.lives.remaining)
        }
}
