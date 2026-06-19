package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplate
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate

class SqliteChallengeTemplates(
    private val jdbc: JdbcTemplate,
) : ChallengeTemplates {
    override suspend fun save(template: ChallengeTemplate): ChallengeTemplate =
        withContext(Dispatchers.IO) {
            jdbc.update(
                "INSERT OR REPLACE INTO challenge_template (id, difficulty, prompt, picture_url) VALUES (?, ?, ?, ?)",
                template.id,
                template.difficulty.name,
                template.prompt.text,
                template.pictureUrl,
            )
            template
        }

    override suspend fun randomReady(difficulty: Difficulty): ChallengeTemplate? =
        withContext(Dispatchers.IO) {
            jdbc
                .query(
                    "SELECT id, difficulty, prompt, picture_url FROM challenge_template WHERE difficulty = ? ORDER BY RANDOM() LIMIT 1",
                    { rs, _ ->
                        ChallengeTemplate(
                            rs.getString("id"),
                            Difficulty.valueOf(rs.getString("difficulty")),
                            Prompt(rs.getString("prompt")),
                            rs.getString("picture_url"),
                        )
                    },
                    difficulty.name,
                ).firstOrNull()
        }

    override suspend fun count(difficulty: Difficulty): Int =
        withContext(Dispatchers.IO) {
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM challenge_template WHERE difficulty = ?",
                Int::class.java,
                difficulty.name,
            ) ?: 0
        }
}
