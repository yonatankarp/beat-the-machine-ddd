package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import org.springframework.jdbc.core.JdbcTemplate

class SqliteFindChallengeById(
    private val jdbc: JdbcTemplate,
    private val mapper: ChallengeRowMapper,
) : FindChallengeById {
    override fun invoke(id: ChallengeId): Challenge? =
        jdbc
            .query("SELECT * FROM challenge WHERE id = ?", mapper.rowMapper, id.value.toString())
            .firstOrNull()
            ?.let { mapper.toDomain(it) }
}
