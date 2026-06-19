package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

class SeedMachine : Machine {
    override suspend fun generate(prompt: Prompt): Picture {
        val url = SEED.find { it.first == prompt }?.second
        return if (url != null) Picture.Ready(url) else Picture.Failed
    }
}
