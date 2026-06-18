package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty

fun interface StartChallenge {
    operator fun invoke(difficulty: Difficulty): Challenge
}
