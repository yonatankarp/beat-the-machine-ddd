package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val ChallengeSuite by testSuite {
    test("a correct guess reveals the word and stays in progress") {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "hello".asGuess()

        // When
        val (updated, outcome) = challenge.makeGuess(guess)

        // Then
        outcome shouldBe GuessOutcome.HIT
        updated.status shouldBe ChallengeStatus.IN_PROGRESS
        updated.lives.remaining shouldBe 3
    }

    test("a wrong guess costs a life") {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "nope".asGuess()

        // When
        val (updated, outcome) = challenge.makeGuess(guess)

        // Then
        outcome shouldBe GuessOutcome.MISS
        updated.lives.remaining shouldBe 2
    }

    test("guessing every word beats the machine") {
        // Given
        val challenge = mediumChallenge()
        val (afterFirst, _) = challenge.makeGuess("hello".asGuess())

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess("world".asGuess())

        // Then
        outcome shouldBe GuessOutcome.HIT
        afterSecond.status shouldBe ChallengeStatus.BEATEN
    }

    test("running out of lives loses the challenge") {
        // Given
        val challenge = mediumChallenge(lives = 1.lives())
        val guess = "nope".asGuess()

        // When
        val (updated, _) = challenge.makeGuess(guess)

        // Then
        updated.status shouldBe ChallengeStatus.LOST
    }

    test("a duplicate guess is a no-op and costs no life") {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val (afterFirst, _) = challenge.makeGuess("nope".asGuess())

        // When
        val (afterSecond, outcome) = afterFirst.makeGuess("Nope".asGuess())

        // Then
        outcome shouldBe GuessOutcome.DUPLICATE
        afterSecond.lives.remaining shouldBe 2
    }

    test("makeGuess does not mutate the receiver") {
        // Given
        val challenge = mediumChallenge(lives = 3.lives())
        val guess = "nope".asGuess()

        // When
        challenge.makeGuess(guess)

        // Then
        challenge.lives.remaining shouldBe 3
        challenge.guesses.isEmpty().shouldBeTrue()
    }

    test("guessing after the challenge is over is rejected") {
        // Given
        val (lost, _) = mediumChallenge(lives = 1.lives()).makeGuess("nope".asGuess())

        // When / Then
        shouldThrow<ChallengeAlreadyOver> { lost.makeGuess("hello".asGuess()) }
    }

    test("forfeit reveals the prompt and loses") {
        // Given
        val challenge = mediumChallenge()

        // When
        val forfeited = challenge.forfeit()

        // Then
        forfeited.status shouldBe ChallengeStatus.LOST
        forfeited
            .maskedPrompt()
            .tokens
            .all { it is MaskedToken.Revealed }
            .shouldBeTrue()
    }

    test("forfeit after the challenge is over is rejected") {
        // Given
        val forfeited = mediumChallenge().forfeit()

        // When / Then
        shouldThrow<ChallengeAlreadyOver> { forfeited.forfeit() }
    }

    test("withPicture returns an independent copy at the same version") {
        // Given
        val challenge = mediumChallenge()
        val newPicture = readyPicture("https://example.com/img.png")

        // When
        val updated = challenge.withPicture(newPicture)

        // Then
        updated.picture shouldBe newPicture
        updated.version shouldBe challenge.version
        challenge.picture shouldBe Picture.Pending
        (challenge !== updated).shouldBeTrue()
    }
}
