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
import com.yonatankarp.testballoon.gwt.given
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
        whenever("starting a challenge") {
            then("it serves the template instantly without enqueuing a picture") {
                val templates = mockk<ChallengeTemplates>()
                val promptSource = mockk<PromptSource>()
                val store = FakeChallengeStore()
                val enqueued = mutableListOf<ChallengeId>()
                val replenished = mutableListOf<Difficulty>()
                coEvery { templates.randomReady(Difficulty.EASY) } returns
                    ChallengeTemplate("t1", Difficulty.EASY, Prompt("red car"), "/images/a")
                val startChallenge =
                    StartChallengeUseCase(templates, promptSource, store, { enqueued += it }, { replenished += it })

                val challenge = startChallenge handle StartChallenge.Command(Difficulty.EASY)

                challenge.picture shouldBe Picture.Ready("/images/a")
                challenge.lives shouldBe Lives.forSecret(Prompt("red car"), Difficulty.EASY)
                enqueued.shouldBeEmpty()
                replenished shouldBe listOf(Difficulty.EASY)
                store.byId.containsKey(challenge.id).shouldBeTrue()
            }
        }
    }

    given("an empty pool") {
        whenever("starting a challenge") {
            then("it falls back to on-demand generation") {
                val templates = mockk<ChallengeTemplates>()
                val promptSource = mockk<PromptSource>()
                val store = FakeChallengeStore()
                val enqueued = mutableListOf<ChallengeId>()
                val replenished = mutableListOf<Difficulty>()
                coEvery { templates.randomReady(Difficulty.HARD) } returns null
                coEvery { promptSource answer PromptSource.Query(Difficulty.HARD) } returns Prompt("a b c")
                val startChallenge =
                    StartChallengeUseCase(templates, promptSource, store, { enqueued += it }, { replenished += it })

                val challenge = startChallenge handle StartChallenge.Command(Difficulty.HARD)

                challenge.picture shouldBe Picture.Pending
                enqueued shouldBe listOf(challenge.id)
                replenished shouldBe listOf(Difficulty.HARD)
            }
        }
    }
}
