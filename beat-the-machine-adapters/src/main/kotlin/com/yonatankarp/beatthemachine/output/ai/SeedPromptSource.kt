package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlin.random.Random

class SeedPromptSource : PromptSource {
    override suspend fun next(difficulty: Difficulty): Prompt = SEED[Random.nextInt(SEED.size)].first
}
