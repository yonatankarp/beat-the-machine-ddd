package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

class SqlitePictureStore(
    private val jdbc: JdbcTemplate,
) : PictureStore {
    override suspend fun save(
        bytes: ByteArray,
        contentType: String,
    ): String =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            jdbc.update(
                "INSERT OR REPLACE INTO picture (id, bytes, content_type) VALUES (?, ?, ?)",
                id,
                bytes,
                contentType,
            )
            id
        }

    override suspend fun load(id: String): StoredImage? =
        withContext(Dispatchers.IO) {
            jdbc
                .query(
                    "SELECT bytes, content_type FROM picture WHERE id = ?",
                    { rs, _ -> StoredImage(rs.getBytes("bytes"), rs.getString("content_type")) },
                    id,
                ).firstOrNull()
        }
}
