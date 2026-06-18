package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import org.springframework.jdbc.core.JdbcTemplate

class SqliteFindPendingChallenges(
    private val jdbc: JdbcTemplate,
    private val mapper: ChallengeRowMapper,
) : FindPendingChallenges {
    override fun invoke(): List<Challenge> =
        jdbc
            .query("SELECT * FROM challenge WHERE picture_status = ?", mapper.rowMapper, "PENDING")
            .map { mapper.toDomain(it) }
}
