package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate

class SqliteFindPendingChallenges(
    private val jdbc: JdbcTemplate,
    private val mapper: ChallengeRowMapper,
) : FindPendingChallenges {
    override suspend fun invoke(): List<Challenge> =
        withContext(Dispatchers.IO) {
            jdbc
                .query("SELECT * FROM challenge WHERE picture_status = ?", mapper.rowMapper, "PENDING")
                .map { mapper.toDomain(it) }
        }
}
