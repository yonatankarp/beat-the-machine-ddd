package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeTemplates
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val ChallengePoolReplenisherSuite by testSuite {
    given("a replenisher with a working machine") {
        whenever("replenishing a difficulty") {
            then("it reports pool progress for capacity planning") {
                val templates: ChallengeTemplates = InMemoryChallengeTemplates()
                val promptSource = mockk<PromptSource>()
                val machine = mockk<Machine>()
                val events = mutableListOf<PoolReplenishmentEvent>()
                coEvery { promptSource answer PromptSource.Query(Difficulty.EASY) } returns Prompt("red car")
                coEvery { machine answer any() } returns Picture.Ready("/images/x")
                val replenisher =
                    ChallengePoolReplenisher(
                        promptSource,
                        machine,
                        templates,
                        CoroutineScope(Dispatchers.Unconfined),
                        target = 2,
                        observe = events::add,
                    ) {
                        "id-${templates.hashCode()}-${System.nanoTime()}"
                    }

                replenisher.replenish(Difficulty.EASY)

                events shouldBe
                    listOf(
                        PoolReplenishmentEvent.Started(Difficulty.EASY, current = 0, target = 2, deficit = 2),
                        PoolReplenishmentEvent.Generated(Difficulty.EASY, current = 1, target = 2, remaining = 1),
                        PoolReplenishmentEvent.Generated(Difficulty.EASY, current = 2, target = 2, remaining = 0),
                        PoolReplenishmentEvent.Finished(Difficulty.EASY, current = 2, target = 2),
                    )
            }
        }

        whenever("replenishing a difficulty below target") {
            then("it fills the difficulty up to target") {
                val templates: ChallengeTemplates = InMemoryChallengeTemplates()
                val promptSource = mockk<PromptSource>()
                val machine = mockk<Machine>()
                coEvery { promptSource answer PromptSource.Query(Difficulty.EASY) } returns Prompt("red car")
                coEvery { machine answer any() } returns Picture.Ready("/images/x")
                val replenisher =
                    ChallengePoolReplenisher(promptSource, machine, templates, CoroutineScope(Dispatchers.Unconfined), target = 3) {
                        "id-${templates.hashCode()}-${System.nanoTime()}"
                    }

                replenisher.replenish(Difficulty.EASY)

                templates.count(Difficulty.EASY) shouldBe 3
            }
        }

        whenever("replenishing a difficulty already at target") {
            then("it does not over-fill") {
                val templates: ChallengeTemplates = InMemoryChallengeTemplates()
                val promptSource = mockk<PromptSource>()
                val machine = mockk<Machine>()
                coEvery { promptSource answer PromptSource.Query(Difficulty.MEDIUM) } returns Prompt("blue boat")
                coEvery { machine answer any() } returns Picture.Ready("/images/y")
                val replenisher =
                    ChallengePoolReplenisher(promptSource, machine, templates, CoroutineScope(Dispatchers.Unconfined), target = 1) {
                        "id-${templates.hashCode()}-${System.nanoTime()}"
                    }

                replenisher.replenish(Difficulty.MEDIUM)
                replenisher.replenish(Difficulty.MEDIUM)

                templates.count(Difficulty.MEDIUM) shouldBe 1
            }
        }
    }

    given("a replenisher whose machine fails") {
        whenever("replenishing a difficulty") {
            then("it does not store failed generations") {
                val templates: ChallengeTemplates = InMemoryChallengeTemplates()
                val promptSource = mockk<PromptSource>()
                val machine = mockk<Machine>()
                coEvery { promptSource answer PromptSource.Query(Difficulty.HARD) } returns Prompt("a b c")
                coEvery { machine answer any() } returns Picture.Failed
                val replenisher =
                    ChallengePoolReplenisher(promptSource, machine, templates, CoroutineScope(Dispatchers.Unconfined), target = 2) {
                        "id-${templates.hashCode()}-${System.nanoTime()}"
                    }

                replenisher.replenish(Difficulty.HARD)

                templates.count(Difficulty.HARD) shouldBe 0
            }
        }
    }
}
