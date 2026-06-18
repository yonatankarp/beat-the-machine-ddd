package com.yonatankarp.beatthemachine.application.port.out

import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Prompt

interface PromptSource {
    fun next(difficulty: Difficulty): Prompt
}
