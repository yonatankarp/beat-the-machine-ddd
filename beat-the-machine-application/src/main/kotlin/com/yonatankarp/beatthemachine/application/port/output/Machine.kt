package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

fun interface Machine {
    suspend infix fun generate(prompt: Prompt): Picture
}
