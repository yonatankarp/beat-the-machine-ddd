package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

class SqliteStorePicture(
    private val jdbc: JdbcTemplate,
) : StorePicture {
    override suspend fun handle(command: StorePicture.Command): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            jdbc.update(
                "INSERT OR REPLACE INTO picture (id, bytes, content_type) VALUES (?, ?, ?)",
                id,
                command.bytes,
                command.contentType,
            )
            id
        }
}
