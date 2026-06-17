package com.yonatankarp.beatthemachine.out.ai

import com.yonatankarp.beatthemachine.application.port.out.PromptSource
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Prompt
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class SeedPromptSource : PromptSource {
    override fun next(difficulty: Difficulty): Prompt = SEED[Random.nextInt(SEED.size)].first
}
