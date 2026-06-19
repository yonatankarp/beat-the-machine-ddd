package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeTemplates
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChallengePoolReplenisherTest {
    private val promptSource = mockk<com.yonatankarp.beatthemachine.application.port.output.PromptSource>()
    private val machine = mockk<com.yonatankarp.beatthemachine.application.port.output.Machine>()
    private val templates: ChallengeTemplates = InMemoryChallengeTemplates()

    private fun replenisher(
        scope: CoroutineScope,
        target: Int,
    ) = ChallengePoolReplenisher(promptSource, machine, templates, scope, target) { "id-${templates.hashCode()}-${System.nanoTime()}" }

    @Test
    fun `fills the difficulty up to target`() =
        runTest {
            coEvery { promptSource.next(Difficulty.EASY) } returns Prompt("red car")
            coEvery { machine.generate(any()) } returns Picture.Ready("/images/x")
            val r = replenisher(CoroutineScope(Dispatchers.Unconfined), target = 3)

            r.replenish(Difficulty.EASY)

            assertEquals(3, templates.count(Difficulty.EASY))
        }

    @Test
    fun `does not store failed generations`() =
        runTest {
            coEvery { promptSource.next(Difficulty.HARD) } returns Prompt("a b c")
            coEvery { machine.generate(any()) } returns Picture.Failed
            val r = replenisher(CoroutineScope(Dispatchers.Unconfined), target = 2)

            r.replenish(Difficulty.HARD)

            assertEquals(0, templates.count(Difficulty.HARD))
        }

    @Test
    fun `no-op when already at target`() =
        runTest {
            coEvery { promptSource.next(Difficulty.MEDIUM) } returns Prompt("blue boat")
            coEvery { machine.generate(any()) } returns Picture.Ready("/images/y")
            val r = replenisher(CoroutineScope(Dispatchers.Unconfined), target = 1)

            r.replenish(Difficulty.MEDIUM)
            r.replenish(Difficulty.MEDIUM)

            assertEquals(1, templates.count(Difficulty.MEDIUM))
        }
}
