package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlin.random.Random

/**
 * Returns a random prompt from the curated [SEED] set. Difficulty does not yet
 * influence selection (there is a single seed pool); the parameter is honoured by
 * the port contract for when a difficulty-aware source replaces this seed adapter.
 */
class SeedPromptSource : PromptSource {
    override fun next(difficulty: Difficulty): Prompt = SEED[Random.nextInt(SEED.size)].first
}
