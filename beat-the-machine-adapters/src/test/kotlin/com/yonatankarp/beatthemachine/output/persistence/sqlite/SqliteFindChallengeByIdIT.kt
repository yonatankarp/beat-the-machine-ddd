package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SqliteFindChallengeByIdIT {
    private lateinit var storeChallenge: SqliteStoreChallenge
    private lateinit var findChallengeById: SqliteFindChallengeById

    @BeforeEach
    fun setup() {
        val jdbc = newSqliteJdbc()
        val mapper = ChallengeRowMapper()
        storeChallenge = SqliteStoreChallenge(jdbc, mapper)
        findChallengeById = SqliteFindChallengeById(jdbc, mapper)
    }

    @Test
    fun `finds a stored challenge with its fields intact`() {
        val c = Challenge.start(Prompt("pixel art cat"), Lives(5))
        storeChallenge(c)

        val found = findChallengeById(c.id)
        assertNotNull(found)
        assertEquals(c.id, found.id)
        assertEquals("pixel art cat", found.secretPrompt().text)
        assertEquals(5, found.lives.remaining)
    }

    @Test
    fun `returns null for an unknown id`() {
        assertNull(findChallengeById(ChallengeId.new()))
    }
}
