package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

val StartChallengeUseSuite by testSuite {
    given("a ready template in the pool") {
        val templates by setup {
            mockk<ChallengeTemplates>().also {
                coEvery { it.randomReady(Difficulty.EASY) } returns
                    ChallengeTemplate("t1", Difficulty.EASY, Prompt("red car"), "/images/a")
            }
        }
        val promptSource by setup { mockk<PromptSource>() }
        val store by setup { FakeChallengeStore() }
        val enqueued by setup { mutableListOf<ChallengeId>() }
        val replenished by setup { mutableListOf<Difficulty>() }
        val startChallenge by setup {
            StartChallengeUseCase(templates, promptSource, store, { enqueued += it }, { replenished += it })
        }

        whenever("starting a challenge") {
            val challenge by action { startChallenge handle StartChallenge.Command(Difficulty.EASY) }

            then("it serves the template instantly without enqueuing a picture") {
                challenge.picture shouldBe Picture.Ready("/images/a")
                challenge.lives shouldBe Lives.forSecret(Prompt("red car"), Difficulty.EASY)
                enqueued.shouldBeEmpty()
                replenished shouldBe listOf(Difficulty.EASY)
                store.byId.containsKey(challenge.id).shouldBeTrue()
            }
        }
    }

    given("an empty pool") {
        val templates by setup {
            mockk<ChallengeTemplates>().also {
                coEvery { it.randomReady(Difficulty.HARD) } returns null
            }
        }
        val promptSource by setup {
            mockk<PromptSource>().also {
                coEvery { it answer PromptSource.Query(Difficulty.HARD) } returns Prompt("a b c")
            }
        }
        val store by setup { FakeChallengeStore() }
        val enqueued by setup { mutableListOf<ChallengeId>() }
        val replenished by setup { mutableListOf<Difficulty>() }
        val startChallenge by setup {
            StartChallengeUseCase(templates, promptSource, store, { enqueued += it }, { replenished += it })
        }

        whenever("starting a challenge") {
            val challenge by action { startChallenge handle StartChallenge.Command(Difficulty.HARD) }

            then("it falls back to on-demand generation") {
                challenge.picture shouldBe Picture.Pending
                enqueued shouldBe listOf(challenge.id)
                replenished shouldBe listOf(Difficulty.HARD)
            }
        }
    }
}
