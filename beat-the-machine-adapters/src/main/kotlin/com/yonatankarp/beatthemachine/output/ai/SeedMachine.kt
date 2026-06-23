package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

class SeedMachine : Machine {
    override suspend fun answer(query: Machine.Query): Picture {
        val url = SEED.find { it.prompt == query.prompt }?.pictureUrl
        return if (url != null) Picture.Ready(url) else Picture.Failed
    }
}
