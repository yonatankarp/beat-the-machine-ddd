package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

fun interface PromptSource {
    suspend infix fun next(difficulty: Difficulty): Prompt
}
