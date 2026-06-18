package com.yonatankarp.beatthemachine.output.persistence.sqlite

/**
 * Plain data class used as the persistence mapping layer between the database and
 * the domain Challenge. The domain type is never annotated; this class is consumed
 * only by the [SqliteChallengeRepository] RowMapper.
 *
 * guesses are stored as a pipe-delimited (|) string. The pipe character is chosen
 * because it cannot appear in a valid guess word (guesses are single, non-blank
 * words; pipe is not a letter).
 */
data class ChallengeRow(
    val id: String,
    val prompt: String,
    val guesses: String,
    val lives: Int,
    val status: String,
    val pictureStatus: String,
    val pictureUrl: String?,
    val difficulty: String,
    val version: Long,
)
