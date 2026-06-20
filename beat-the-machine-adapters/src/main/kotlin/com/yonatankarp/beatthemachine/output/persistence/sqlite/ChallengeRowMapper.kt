package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Guess
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.springframework.jdbc.core.RowMapper
import java.util.UUID

class ChallengeRowMapper {
    val rowMapper: RowMapper<ChallengeRow> =
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

    fun toRow(
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

    fun toDomain(row: ChallengeRow): Challenge {
        val picture =
            when (row.pictureStatus) {
                "READY" -> Picture.Ready(row.pictureUrl ?: throw IllegalStateException("READY picture row ${row.id} has null url"))
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
        const val GUESS_DELIMITER = "|"
    }
}
