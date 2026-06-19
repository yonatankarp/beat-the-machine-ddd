package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import java.util.UUID

class ChallengeTemplateSeeder(
    private val templates: ChallengeTemplates,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun seed() {
        Difficulty.entries.forEach { difficulty ->
            if ((templates count difficulty) == 0) {
                SEED.forEach { (prompt, url) ->
                    templates save ChallengeTemplate(idFactory(), difficulty, prompt, url)
                }
            }
        }
    }
}
