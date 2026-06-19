package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StartChallengeUseCaseTest {
    private val templates = mockk<ChallengeTemplates>()
    private val promptSource = mockk<PromptSource>()
    private val store = FakeChallengeStore()
    private val enqueued = mutableListOf<ChallengeId>()
    private val replenished = mutableListOf<Difficulty>()

    private fun useCase() = StartChallengeUseCase(templates, promptSource, store, { enqueued += it }, { replenished += it })

    @Test
    fun `serves a ready template instantly without enqueuing a picture`() =
        runTest {
            coEvery { templates.randomReady(Difficulty.EASY) } returns
                ChallengeTemplate("t1", Difficulty.EASY, Prompt("red car"), "/images/a")

            val challenge = useCase().invoke(Difficulty.EASY)

            assertEquals(Picture.Ready("/images/a"), challenge.picture)
            assertEquals(Lives.forSecret(Prompt("red car"), Difficulty.EASY), challenge.lives)
            assertTrue(enqueued.isEmpty())
            assertEquals(listOf(Difficulty.EASY), replenished)
        }

    @Test
    fun `falls back to on-demand generation when the pool is empty`() =
        runTest {
            coEvery { templates.randomReady(Difficulty.HARD) } returns null
            coEvery { promptSource.next(Difficulty.HARD) } returns Prompt("a b c")

            val challenge = useCase().invoke(Difficulty.HARD)

            assertEquals(Picture.Pending, challenge.picture)
            assertEquals(listOf(challenge.id), enqueued)
            assertEquals(listOf(Difficulty.HARD), replenished)
        }
}
