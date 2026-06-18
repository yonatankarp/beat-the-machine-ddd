package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SqliteFindPendingChallengesIT {
    private lateinit var storeChallenge: SqliteStoreChallenge
    private lateinit var findPendingChallenges: SqliteFindPendingChallenges

    @BeforeEach
    fun setup() {
        val jdbc = newSqliteJdbc()
        val mapper = ChallengeRowMapper()
        storeChallenge = SqliteStoreChallenge(jdbc, mapper)
        findPendingChallenges = SqliteFindPendingChallenges(jdbc, mapper)
    }

    @Test
    fun `returns only challenges whose picture is pending`() {
        val pendingA = storeChallenge(Challenge.start(Prompt("pending one"), Lives(2), picture = Picture.Pending))
        val pendingB = storeChallenge(Challenge.start(Prompt("pending two"), Lives(2), picture = Picture.Pending))
        storeChallenge(Challenge.start(Prompt("ready pic"), Lives(2), picture = Picture.Ready("https://example.com/img.png")))
        storeChallenge(Challenge.start(Prompt("failed pic"), Lives(2), picture = Picture.Failed))

        val ids = findPendingChallenges().map { it.id }.toSet()
        assertEquals(setOf(pendingA.id, pendingB.id), ids)
    }
}
