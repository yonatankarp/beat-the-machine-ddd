package com.yonatankarp.beatthemachine.out.persistence.sqlite

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Spring Data JDBC entity. The domain [Challenge][com.yonatankarp.beatthemachine.domain.Challenge]
 * is never annotated; this row class acts as the mapping layer.
 *
 * guesses are stored as a pipe-delimited (|) string. The pipe character is
 * chosen because it cannot appear in a valid guess word (guesses are single,
 * non-blank words; pipe is not a letter).
 */
@Table("challenge")
data class ChallengeRow(
    @Id val id: String,
    val prompt: String,
    val guesses: String,
    val lives: Int,
    val status: String,
    val pictureStatus: String,
    val pictureUrl: String?,
    val difficulty: String,
    val version: Long,
)
