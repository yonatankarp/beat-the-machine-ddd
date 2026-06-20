package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.JdbcTemplate

class SqliteFindPicture(
    private val jdbc: JdbcTemplate,
) : FindPicture {
    override suspend fun answer(query: FindPicture.Query): StoredImage? =
        withContext(Dispatchers.IO) {
            jdbc
                .query(
                    "SELECT bytes, content_type FROM picture WHERE id = ?",
                    { rs, _ -> StoredImage(rs.getBytes("bytes"), rs.getString("content_type")) },
                    query.id,
                ).firstOrNull()
        }
}
