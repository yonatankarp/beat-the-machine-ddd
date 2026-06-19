package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.output.persistence.sqlite.ChallengeRowMapper
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteChallengeTemplates
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteFindChallengeById
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteFindPendingChallenges
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteFindPicture
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteStoreChallenge
import com.yonatankarp.beatthemachine.output.persistence.sqlite.SqliteStorePicture
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
@ConditionalOnProperty(name = ["btm.persistence"], havingValue = "sqlite")
class SqlitePersistenceConfig {
    @Bean
    fun challengeRowMapper(): ChallengeRowMapper = ChallengeRowMapper()

    @Bean
    fun storeChallenge(
        jdbcTemplate: JdbcTemplate,
        mapper: ChallengeRowMapper,
    ): StoreChallenge = SqliteStoreChallenge(jdbcTemplate, mapper)

    @Bean
    fun findChallengeById(
        jdbcTemplate: JdbcTemplate,
        mapper: ChallengeRowMapper,
    ): FindChallengeById = SqliteFindChallengeById(jdbcTemplate, mapper)

    @Bean
    fun findPendingChallenges(
        jdbcTemplate: JdbcTemplate,
        mapper: ChallengeRowMapper,
    ): FindPendingChallenges = SqliteFindPendingChallenges(jdbcTemplate, mapper)

    @Bean
    fun storePicture(jdbcTemplate: JdbcTemplate): StorePicture = SqliteStorePicture(jdbcTemplate)

    @Bean
    fun findPicture(jdbcTemplate: JdbcTemplate): FindPicture = SqliteFindPicture(jdbcTemplate)

    @Bean
    fun challengeTemplates(jdbcTemplate: JdbcTemplate): ChallengeTemplates = SqliteChallengeTemplates(jdbcTemplate)
}
