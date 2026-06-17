package com.yonatankarp.beatthemachine.out.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.out.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Picture
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SqliteChallengeRepositoryIT {
    private lateinit var repo: SqliteChallengeRepository

    @BeforeEach
    fun setup() {
        // SingleConnectionDataSource keeps one connection open for the test,
        // which is required for SQLite in-memory DBs (each new connection gets
        // a fresh empty database).
        val ds = SingleConnectionDataSource("jdbc:sqlite::memory:", true)
        ds.setDriverClassName("org.sqlite.JDBC")
        val jdbc = JdbcTemplate(ds)
        jdbc.execute(
            """
            CREATE TABLE IF NOT EXISTS challenge (
                id             TEXT PRIMARY KEY,
                prompt         TEXT NOT NULL,
                guesses        TEXT NOT NULL,
                lives          INT  NOT NULL,
                status         TEXT NOT NULL,
                picture_status TEXT NOT NULL,
                picture_url    TEXT,
                difficulty     TEXT NOT NULL,
                version        INT  NOT NULL
            )
            """.trimIndent(),
        )
        repo = SqliteChallengeRepository(jdbc)
    }

    @Test
    fun `saves and finds a fresh challenge`() {
        val c = Challenge.start(Prompt("pixel art cat"), Lives(5))
        val saved = repo.save(c)

        assertEquals(1L, saved.version)
        val found = repo.findById(c.id)
        assertNotNull(found)
        assertEquals(c.id, found.id)
        assertEquals("pixel art cat", found.secretPrompt().text)
        assertEquals(5, found.lives.remaining)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val unknown = Challenge.start(Prompt("ghost"), Lives(1))
        assertNull(repo.findById(unknown.id))
    }

    @Test
    fun `rejects a stale version on second save`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        repo.save(c) // stored version becomes 1
        assertFailsWith<OptimisticLockConflict> { repo.save(c) }
    }

    @Test
    fun `allows sequential saves with updated versions`() {
        val c = Challenge.start(Prompt("sequential"), Lives(3))
        val v1 = repo.save(c)
        assertEquals(1L, v1.version)
        val v2 = repo.save(v1)
        assertEquals(2L, v2.version)
    }

    @Test
    fun `persists all difficulty levels`() {
        Difficulty.entries.forEach { diff ->
            val c = Challenge.start(Prompt("test prompt"), Lives(2), difficulty = diff)
            val saved = repo.save(c)
            val found = repo.findById(c.id)
            assertNotNull(found)
            assertEquals(diff, found.difficulty)
            assertEquals(diff, saved.difficulty)
        }
    }

    @Test
    fun `persists picture states correctly`() {
        val pending = Challenge.start(Prompt("pending pic"), Lives(2), picture = Picture.Pending)
        val ready = Challenge.start(Prompt("ready pic"), Lives(2), picture = Picture.Ready("https://example.com/img.png"))
        val failed = Challenge.start(Prompt("failed pic"), Lives(2), picture = Picture.Failed)

        repo.save(pending)
        repo.save(ready)
        repo.save(failed)

        assertEquals(Picture.Pending, repo.findById(pending.id)?.picture)
        assertEquals(Picture.Ready("https://example.com/img.png"), repo.findById(ready.id)?.picture)
        assertEquals(Picture.Failed, repo.findById(failed.id)?.picture)
    }
}
