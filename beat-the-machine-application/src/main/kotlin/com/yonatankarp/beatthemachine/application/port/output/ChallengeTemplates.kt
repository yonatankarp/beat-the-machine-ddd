package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

data class ChallengeTemplate(
    val id: String,
    val difficulty: Difficulty,
    val prompt: Prompt,
    val pictureUrl: String,
)

interface ChallengeTemplates {
    suspend infix fun save(template: ChallengeTemplate): ChallengeTemplate

    suspend infix fun randomReady(difficulty: Difficulty): ChallengeTemplate?

    suspend infix fun count(difficulty: Difficulty): Int
}
