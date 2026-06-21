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
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk

val StartChallengeUseSuite by testSuite {
    val fakePrompt = "hello world".asPrompt()
    val prompts = mockk<PromptSource>().also { coEvery { it answer any() } returns fakePrompt }

    test("starts a pending challenge and enqueues picture generation") {
        val store = FakeChallengeStore()
        val enqueued = mutableListOf<ChallengeId>()
        val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }
        val challenge = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)
        challenge.picture shouldBe Picture.Pending
        challenge.status shouldBe ChallengeStatus.IN_PROGRESS
        enqueued shouldBe listOf(challenge.id)
        store.byId.containsKey(challenge.id).shouldBeTrue()
    }

    test("starting lives scale with difficulty") {
        val store = FakeChallengeStore()
        val startChallenge = StartChallengeUseCase(prompts, store) {}
        val easy = startChallenge handle StartChallenge.Command(Difficulty.EASY)
        val medium = startChallenge handle StartChallenge.Command(Difficulty.MEDIUM)
        val hard = startChallenge handle StartChallenge.Command(Difficulty.HARD)
        easy.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.EASY).remaining
        medium.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.MEDIUM).remaining
        hard.lives.remaining shouldBe Lives.forSecret(fakePrompt, Difficulty.HARD).remaining
    }

    test("StoredImage equality is content-based") {
        val a = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val b = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val c = StoredImage(byteArrayOf(9), "image/png")
        a shouldBe b
        a shouldNotBe c
        a.hashCode() shouldBe b.hashCode()
    }
}
