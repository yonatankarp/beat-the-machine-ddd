package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

val StartChallengeUseSuite by testSuite {
    val fakePrompt = "hello world".asPrompt()
    val prompts = mockk<PromptSource>().also { coEvery { it answer any() } returns fakePrompt }

    given("a prompt source and store") {
        whenever("starting a medium challenge") {
            then("a pending challenge is created and picture generation is enqueued") {
                val store = FakeChallengeStore()
                val enqueued = mutableListOf<ChallengeId>()
                val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }
                val challenge = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)
                challenge.picture shouldBe Picture.Pending
                challenge.status shouldBe ChallengeStatus.IN_PROGRESS
                enqueued shouldBe listOf(challenge.id)
                store.byId.containsKey(challenge.id).shouldBeTrue()
            }
        }

        whenever("starting challenges of each difficulty") {
            then("the starting lives scale with difficulty") {
                val store = FakeChallengeStore()
                val startChallenge = StartChallengeUseCase(prompts, store) {}
                val easy = startChallenge handle StartChallenge.Command(Difficulty.EASY)
                val medium = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)
                val hard = startChallenge handle StartChallenge.Command(Difficulty.HARD)
                easy.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.EASY).remaining
                medium.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.MEDIUM).remaining
                hard.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.HARD).remaining
            }
        }
    }
}
