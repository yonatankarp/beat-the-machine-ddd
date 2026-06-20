package com.yonatankarp.beatthemachine.output.persistence.sqlite

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

internal fun newSqliteJdbc(): JdbcTemplate {
    val ds = SingleConnectionDataSource("jdbc:sqlite::memory:", true)
    ds.setDriverClassName("org.sqlite.JDBC")
    val jdbc = JdbcTemplate(ds)
    val sql =
        (
            ChallengeRowMapper::class.java.getResource("/schema.sql")
                ?: error("schema.sql not found on classpath")
        ).readText()
    sql
        .split(";")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .forEach { jdbc.execute(it) }
    return jdbc
}
