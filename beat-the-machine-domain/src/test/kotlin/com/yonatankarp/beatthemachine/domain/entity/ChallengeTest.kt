package com.yonatankarp.beatthemachine.domain.entity

import com.yonatankarp.beatthemachine.domain.exception.ChallengeAlreadyOver
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.GuessOutcome
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.MaskedToken
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

val ChallengeSuite by testSuite {
    given("a medium challenge with 3 lives") {
        val challenge = mediumChallenge(lives = 3.lives())

        whenever("a correct guess is made") {
            val (updated, outcome) = challenge.makeGuess("hello".asGuess())
            then("the outcome is a hit") {
                outcome shouldBe GuessOutcome.HIT
            }
            then("the challenge stays in progress") {
                updated.status shouldBe ChallengeStatus.IN_PROGRESS
            }
            then("no life is lost") {
                updated.lives.remaining shouldBe 3
            }
        }

        whenever("a wrong guess is made") {
            val (updated, outcome) = challenge.makeGuess("nope".asGuess())
            then("the outcome is a miss") {
                outcome shouldBe GuessOutcome.MISS
            }
            then("one life is lost") {
                updated.lives.remaining shouldBe 2
            }
        }

        whenever("a duplicate guess is made") {
            val (afterFirst, _) = challenge.makeGuess("nope".asGuess())
            val (afterSecond, outcome) = afterFirst.makeGuess("Nope".asGuess())
            then("the outcome is a duplicate") {
                outcome shouldBe GuessOutcome.DUPLICATE
            }
            then("no additional life is lost") {
                afterSecond.lives.remaining shouldBe 2
            }
        }

        whenever("a guess is made") {
            challenge.makeGuess("nope".asGuess())
            then("the receiver lives are not mutated") {
                challenge.lives.remaining shouldBe 3
            }
            then("the receiver guesses are not mutated") {
                challenge.guesses.isEmpty().shouldBeTrue()
            }
        }
    }

    given("a medium challenge with 1 life") {
        whenever("a wrong guess is made") {
            val (updated, _) = mediumChallenge(lives = 1.lives()).makeGuess("nope".asGuess())
            then("status is LOST") {
                updated.status shouldBe ChallengeStatus.LOST
            }
        }
    }

    given("guessing every word") {
        whenever("all words are guessed") {
            val challenge = mediumChallenge()
            val (afterFirst, _) = challenge.makeGuess("hello".asGuess())
            val (afterSecond, outcome) = afterFirst.makeGuess("world".asGuess())
            then("the outcome is a hit") {
                outcome shouldBe GuessOutcome.HIT
            }
            then("the status is beaten") {
                afterSecond.status shouldBe ChallengeStatus.BEATEN
            }
        }
    }

    given("a finished challenge") {
        whenever("makeGuess is called after the challenge is over") {
            then("it is rejected") {
                val (lost, _) = mediumChallenge(lives = 1.lives()).makeGuess("nope".asGuess())
                shouldThrow<ChallengeAlreadyOver> { lost.makeGuess("hello".asGuess()) }
            }
        }
        whenever("forfeit is called after the challenge is over") {
            then("it is rejected") {
                val forfeited = mediumChallenge().forfeit()
                shouldThrow<ChallengeAlreadyOver> { forfeited.forfeit() }
            }
        }
    }

    given("a medium challenge") {
        whenever("forfeited") {
            val forfeited = mediumChallenge().forfeit()
            then("status is LOST") {
                forfeited.status shouldBe ChallengeStatus.LOST
            }
            then("all tokens are revealed") {
                forfeited
                    .maskedPrompt()
                    .tokens
                    .all { it is MaskedToken.Revealed }
                    .shouldBeTrue()
            }
        }

        whenever("maskedPrompt after one hit") {
            val (afterHit, _) = mediumChallenge().makeGuess("hello".asGuess())
            then("the first token is revealed") {
                val masked = afterHit.maskedPrompt()
                masked.tokens[0] shouldBe MaskedToken.Revealed("hello")
            }
            then("the second token is hidden") {
                val masked = afterHit.maskedPrompt()
                masked.tokens[1] shouldBe MaskedToken.Hidden(5)
            }
        }

        whenever("maxLives is queried") {
            then("derives from secret and difficulty") {
                mediumChallenge().maxLives() shouldBe Lives(6)
            }
        }

        whenever("withPicture is called") {
            val challenge = mediumChallenge()
            val newPicture = readyPicture("https://example.com/img.png")
            val updated = challenge.withPicture(newPicture)
            then("the picture is updated") {
                updated.picture shouldBe newPicture
            }
            then("the version is unchanged") {
                updated.version shouldBe challenge.version
            }
            then("the original picture is still pending") {
                challenge.picture shouldBe Picture.Pending
            }
            then("the updated challenge is a new instance") {
                (challenge !== updated).shouldBeTrue()
            }
        }
    }

    given("Challenge.start") {
        whenever("called with a prompt and lives") {
            val challenge = Challenge.start("hello world".asPrompt(), 6.lives())
            then("the picture defaults to pending") {
                challenge.picture shouldBe Picture.Pending
            }
            then("the difficulty defaults to medium") {
                challenge.difficulty shouldBe Difficulty.MEDIUM
            }
            then("the status is in progress") {
                challenge.status shouldBe ChallengeStatus.IN_PROGRESS
            }
        }
    }

    given("Challenge.rehydrate") {
        whenever("called with full state") {
            val prompt = "secret phrase".asPrompt()
            val challenge =
                Challenge.rehydrate(
                    id = aChallengeId(),
                    prompt = prompt,
                    guesses = emptySet(),
                    lives = 4.lives(),
                    status = ChallengeStatus.IN_PROGRESS,
                    picture = Picture.Failed,
                    difficulty = Difficulty.HARD,
                    version = 7L,
                )
            then("the secret prompt is preserved") {
                challenge.secretPrompt() shouldBe prompt
            }
            then("the lives remaining is preserved") {
                challenge.lives.remaining shouldBe 4
            }
            then("the status is preserved") {
                challenge.status shouldBe ChallengeStatus.IN_PROGRESS
            }
            then("the picture is preserved") {
                challenge.picture shouldBe Picture.Failed
            }
            then("the difficulty is preserved") {
                challenge.difficulty shouldBe Difficulty.HARD
            }
            then("the version is preserved") {
                challenge.version shouldBe 7L
            }
        }
    }
}
