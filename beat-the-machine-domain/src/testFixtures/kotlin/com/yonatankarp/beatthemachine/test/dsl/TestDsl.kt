package com.yonatankarp.beatthemachine.test.dsl

import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

fun String.asPrompt(): Prompt = Prompt(this)

fun String.asGuess(): Guess = Guess(this)

fun Int.lives(): Lives = Lives(this)

fun aChallengeId(): ChallengeId = ChallengeId.new()
