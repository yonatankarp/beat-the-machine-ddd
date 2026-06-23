package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlin.random.Random

class SeedPromptSource : PromptSource {
    override suspend fun answer(query: PromptSource.Query): Prompt {
        val prompts = SEED.filter { it.difficulty == query.difficulty }
        return prompts[Random.nextInt(prompts.size)].prompt
    }
}
