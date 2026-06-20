package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StartChallengeUseCaseTest {
    private val fakePrompt = "hello world".asPrompt()
    private val prompts = mockk<PromptSource>().also { coEvery { it answer any() } returns fakePrompt }
    private val store = FakeChallengeStore()

    @Test
    fun `starts a pending challenge and enqueues picture generation`() =
        runTest {
            // Given
            val enqueued = mutableListOf<ChallengeId>()
            val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }

            // When
            val challenge = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)

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
            val easy = startChallenge handle StartChallenge.Command(Difficulty.EASY)
            val medium = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)
            val hard = startChallenge handle StartChallenge.Command(Difficulty.HARD)

            // Then
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.EASY).remaining, easy.lives.remaining)
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.MEDIUM).remaining, medium.lives.remaining)
            assertEquals(Lives.forSecret(fakePrompt, Difficulty.HARD).remaining, hard.lives.remaining)
        }

    @Test
    fun `StoredImage equality is content-based`() {
        // Given
        val a = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val b = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val c = StoredImage(byteArrayOf(9), "image/png")

        // When / Then
        assertEquals(a, b)
        assertNotEquals(a, c)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
