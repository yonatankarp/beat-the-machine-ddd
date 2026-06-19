package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class InMemoryChallengeTemplates : ChallengeTemplates {
    private val byId = ConcurrentHashMap<String, ChallengeTemplate>()

    override suspend fun save(template: ChallengeTemplate): ChallengeTemplate {
        byId[template.id] = template
        return template
    }

    override suspend fun randomReady(difficulty: Difficulty): ChallengeTemplate? {
        val matches = byId.values.filter { it.difficulty == difficulty }
        return if (matches.isEmpty()) null else matches[Random.nextInt(matches.size)]
    }

    override suspend fun count(difficulty: Difficulty): Int = byId.values.count { it.difficulty == difficulty }
}
