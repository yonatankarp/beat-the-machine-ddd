package com.yonatankarp.beatthemachine.out.ai

import com.yonatankarp.beatthemachine.application.port.out.Machine
import com.yonatankarp.beatthemachine.domain.Picture
import com.yonatankarp.beatthemachine.domain.Prompt

class SeedMachine : Machine {
    override fun generate(prompt: Prompt): Picture {
        val url = SEED.find { it.first.text == prompt.text }?.second
        return if (url != null) Picture.Ready(url) else Picture.Failed
    }
}
