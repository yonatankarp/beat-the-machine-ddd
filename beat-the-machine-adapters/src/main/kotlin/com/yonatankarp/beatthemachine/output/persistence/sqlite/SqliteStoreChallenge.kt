package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate

class SqliteStoreChallenge(
    private val jdbc: JdbcTemplate,
    private val mapper: ChallengeRowMapper,
) : StoreChallenge {
    override suspend fun invoke(challenge: Challenge): Challenge =
        withContext(Dispatchers.IO) {
            val nextVersion = challenge.version + 1
            val row = mapper.toRow(challenge, nextVersion)

            val updated =
                jdbc.update(
                    """
                    UPDATE challenge
                    SET prompt = ?, guesses = ?, lives = ?, status = ?,
                        picture_status = ?, picture_url = ?, difficulty = ?, version = ?
                    WHERE id = ? AND version = ?
                    """.trimIndent(),
                    row.prompt,
                    row.guesses,
                    row.lives,
                    row.status,
                    row.pictureStatus,
                    row.pictureUrl,
                    row.difficulty,
                    nextVersion,
                    row.id,
                    challenge.version,
                )

            if (updated == 0) {
                // Either the record does not yet exist (INSERT) or a concurrent
                // write changed the version (conflict). Disambiguate by presence.
                val exists =
                    jdbc.queryForObject(
                        "SELECT COUNT(*) FROM challenge WHERE id = ?",
                        Int::class.java,
                        row.id,
                    ) ?: 0

                if (exists > 0) {
                    throw OptimisticLockConflict(challenge.id)
                }

                jdbc.update(
                    """
                    INSERT INTO challenge
                        (id, prompt, guesses, lives, status, picture_status, picture_url, difficulty, version)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    row.id,
                    row.prompt,
                    row.guesses,
                    row.lives,
                    row.status,
                    row.pictureStatus,
                    row.pictureUrl,
                    row.difficulty,
                    nextVersion,
                )
            }

            mapper.toDomain(row.copy(version = nextVersion))
        }
}
