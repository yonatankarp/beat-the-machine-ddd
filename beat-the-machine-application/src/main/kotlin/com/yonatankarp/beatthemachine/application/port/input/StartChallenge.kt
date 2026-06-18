package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.Difficulty

interface StartChallenge {
    fun start(difficulty: Difficulty): Challenge
}
