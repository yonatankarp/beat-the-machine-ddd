package com.yonatankarp.beatthemachine.application.port.out

import com.yonatankarp.beatthemachine.domain.Picture
import com.yonatankarp.beatthemachine.domain.Prompt

interface Machine {
    fun generate(prompt: Prompt): Picture
}
