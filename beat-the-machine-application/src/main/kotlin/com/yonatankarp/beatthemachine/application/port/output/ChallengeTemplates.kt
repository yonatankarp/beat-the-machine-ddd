package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

/**
 * A reusable, pre-generated challenge seed: a prompt and its already-rendered
 * picture URL, for a difficulty. Players are instantiated from these for an
 * instant READY challenge; the same template may seed many players.
 */
data class ChallengeTemplate(
    val id: String,
    val difficulty: Difficulty,
    val prompt: Prompt,
    val pictureUrl: String,
)

/** The pool of ready challenge templates, partitioned by difficulty. */
interface ChallengeTemplates {
    suspend fun save(template: ChallengeTemplate): ChallengeTemplate

    /** A random ready template for [difficulty], or null if the pool is empty. */
    suspend fun randomReady(difficulty: Difficulty): ChallengeTemplate?

    suspend fun count(difficulty: Difficulty): Int
}
