package com.yonatankarp.beatthemachine.test.fixtures

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.test.dsl.asGuess
import com.yonatankarp.beatthemachine.test.dsl.asPrompt

object Challenges {
    fun easyChallenge(
        lives: Lives = Lives.initialFor(Difficulty.EASY),
        prompt: Prompt = "hello world".asPrompt(),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.EASY)

    fun mediumChallenge(
        lives: Lives = Lives.initialFor(Difficulty.MEDIUM),
        prompt: Prompt = "hello world".asPrompt(),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.MEDIUM)

    fun hardChallenge(
        lives: Lives = Lives.initialFor(Difficulty.HARD),
        prompt: Prompt = "hello world".asPrompt(),
        picture: Picture = Picture.Pending,
    ): Challenge = Challenge.start(prompt, lives, picture, Difficulty.HARD)

    fun lostChallenge(prompt: Prompt = "hello world".asPrompt()): Challenge = mediumChallenge(prompt = prompt).forfeit()

    fun beatenChallenge(prompt: Prompt = "hello world".asPrompt()): Challenge =
        prompt.words().fold(mediumChallenge(prompt = prompt)) { challenge, word ->
            challenge.makeGuess(word.asGuess()).first
        }
}
