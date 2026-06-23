package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty

class ChallengeTemplateSeeder(
    private val templates: ChallengeTemplates,
) {
    suspend fun seed() {
        Difficulty.entries.forEach { difficulty ->
            if ((templates count difficulty) == 0) {
                SEED.filter { it.difficulty == difficulty }.forEach { seed ->
                    templates save ChallengeTemplate(seed.id, difficulty, seed.prompt, seed.pictureUrl)
                }
            }
        }
    }
}
