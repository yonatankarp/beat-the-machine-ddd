package com.yonatankarp.beatthemachine.test.fixtures

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt

object Challenges {
    fun easyChallenge(
        prompt: Prompt = "hello world".asPrompt(),
        lives: Lives = Lives.forSecret(prompt, Difficulty.EASY),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.EASY)

    fun mediumChallenge(
        prompt: Prompt = "hello world".asPrompt(),
        lives: Lives = Lives.forSecret(prompt, Difficulty.MEDIUM),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.MEDIUM)

    fun mediumChallengeWithGuesses(
        prompt: Prompt = "hello world".asPrompt(),
        guesses: Set<Guess>,
        lives: Lives = Lives.forSecret(prompt, Difficulty.MEDIUM),
        status: ChallengeStatus = ChallengeStatus.IN_PROGRESS,
        picture: Picture = Picture.Pending,
        version: Long = 0,
    ): Challenge =
        Challenge.rehydrate(
            id = mediumChallenge().id,
            prompt = prompt,
            guesses = guesses,
            lives = lives,
            status = status,
            picture = picture,
            difficulty = Difficulty.MEDIUM,
            version = version,
        )

    fun hardChallenge(
        prompt: Prompt = "hello world".asPrompt(),
        lives: Lives = Lives.forSecret(prompt, Difficulty.HARD),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.HARD)

    fun lostChallenge(prompt: Prompt = "hello world".asPrompt()): Challenge = mediumChallenge(prompt = prompt).forfeit()

    fun beatenChallenge(prompt: Prompt = "hello world".asPrompt()): Challenge =
        prompt.words().fold(mediumChallenge(prompt = prompt)) { challenge, word ->
            challenge.makeGuess(word.asGuess()).first
        }
}
