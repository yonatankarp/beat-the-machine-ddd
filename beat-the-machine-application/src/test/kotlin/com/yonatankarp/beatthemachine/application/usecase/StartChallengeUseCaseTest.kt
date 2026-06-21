package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val StartChallengeUseCaseSuite by testSuite {
    val prompts = PromptSource { "hello world".asPrompt() } // stateless, safe at suite scope

    test("starts a pending challenge and enqueues picture generation") {
        // Given
        val store = FakeChallengeStore() // mutable: fresh per test
        val enqueued = mutableListOf<ChallengeId>()
        val startChallenge = StartChallengeUseCase(prompts, store) { enqueued.add(it) }

        // When
        val challenge = startChallenge(Difficulty.MEDIUM)

        // Then
        challenge.picture shouldBe Picture.Pending
        challenge.status shouldBe ChallengeStatus.IN_PROGRESS
        enqueued shouldBe listOf(challenge.id)
        store.byId.containsKey(challenge.id).shouldBeTrue()
    }

    test("starting lives scale with difficulty") {
        // Given
        val store = FakeChallengeStore()
        val startChallenge = StartChallengeUseCase(prompts, store) {}

        // When
        val easy = startChallenge(Difficulty.EASY)
        val medium = startChallenge(Difficulty.MEDIUM)
        val hard = startChallenge(Difficulty.HARD)

        // Then
        easy.lives.remaining shouldBe 8
        medium.lives.remaining shouldBe 6
        hard.lives.remaining shouldBe 4
    }
}
