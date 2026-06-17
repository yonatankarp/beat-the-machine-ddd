package com.yonatankarp.beatthemachine.out.ai

import com.yonatankarp.beatthemachine.application.port.out.PromptSource
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Prompt
import kotlin.random.Random

class SeedPromptSource : PromptSource {
    override fun next(difficulty: Difficulty): Prompt = SEED[Random.nextInt(SEED.size)].first
}
