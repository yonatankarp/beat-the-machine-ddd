package com.yonatankarp.beatthemachine.out.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.port.out.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Guess
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Picture
import com.yonatankarp.beatthemachine.domain.Prompt
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

private const val GUESS_DELIMITER = "|"

class SqliteChallengeRepository(
    private val jdbc: JdbcTemplate,
) : ChallengeRepository {
    override fun save(challenge: Challenge): Challenge {
        val nextVersion = challenge.version + 1
        val row = toRow(challenge, nextVersion)

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
            // write changed the version (conflict). We disambiguate by checking
            // whether the row is present at all.
            val exists =
                jdbc.queryForObject(
                    "SELECT COUNT(*) FROM challenge WHERE id = ?",
                    Int::class.java,
                    row.id,
                ) ?: 0

            if (exists > 0) {
                throw OptimisticLockConflict(challenge.id)
            }

            // No row exists yet — perform the initial INSERT.
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

        return toDomain(row.copy(version = nextVersion))
    }

    override fun findById(id: ChallengeId): Challenge? {
        val results =
            jdbc.query(
                "SELECT * FROM challenge WHERE id = ?",
                ROW_MAPPER,
                id.value.toString(),
            )
        return results.firstOrNull()?.let { toDomain(it) }
    }

    private fun toRow(
        challenge: Challenge,
        version: Long,
    ): ChallengeRow {
        val (pictureStatus, pictureUrl) =
            when (val pic = challenge.picture) {
                is Picture.Pending -> "PENDING" to null
                is Picture.Ready -> "READY" to pic.url
                is Picture.Failed -> "FAILED" to null
            }
        return ChallengeRow(
            id = challenge.id.value.toString(),
            prompt = challenge.secretPrompt().text,
            guesses = challenge.guesses.joinToString(GUESS_DELIMITER) { it.word },
            lives = challenge.lives.remaining,
            status = challenge.status.name,
            pictureStatus = pictureStatus,
            pictureUrl = pictureUrl,
            difficulty = challenge.difficulty.name,
            version = version,
        )
    }

    private fun toDomain(row: ChallengeRow): Challenge {
        val picture =
            when (row.pictureStatus) {
                "READY" -> Picture.Ready(row.pictureUrl ?: "")
                "FAILED" -> Picture.Failed
                else -> Picture.Pending
            }
        val guesses =
            if (row.guesses.isBlank()) {
                emptySet()
            } else {
                row.guesses
                    .split(GUESS_DELIMITER)
                    .map { Guess(it) }
                    .toSet()
            }
        return Challenge.rehydrate(
            id = ChallengeId(UUID.fromString(row.id)),
            prompt = Prompt(row.prompt),
            guesses = guesses,
            lives = Lives(row.lives),
            status = ChallengeStatus.valueOf(row.status),
            picture = picture,
            difficulty = Difficulty.valueOf(row.difficulty),
            version = row.version,
        )
    }

    private companion object {
        val ROW_MAPPER =
            RowMapper { rs, _ ->
                ChallengeRow(
                    id = rs.getString("id"),
                    prompt = rs.getString("prompt"),
                    guesses = rs.getString("guesses"),
                    lives = rs.getInt("lives"),
                    status = rs.getString("status"),
                    pictureStatus = rs.getString("picture_status"),
                    pictureUrl = rs.getString("picture_url"),
                    difficulty = rs.getString("difficulty"),
                    version = rs.getLong("version"),
                )
            }
    }
}
