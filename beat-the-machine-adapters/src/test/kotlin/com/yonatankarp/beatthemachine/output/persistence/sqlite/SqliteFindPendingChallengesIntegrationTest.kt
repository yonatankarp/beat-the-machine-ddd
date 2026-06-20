package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.failedPicture
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SqliteFindPendingChallengesIntegrationTest {
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
    fun `returns only challenges whose picture is pending`() =
        runTest {
            // Given
            val pendingA = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "pending one".asPrompt()))
            val pendingB = storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "pending two".asPrompt()))
            storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "ready pic".asPrompt(), picture = readyPicture()))
            storeChallenge handle StoreChallenge.Command(mediumChallenge(prompt = "failed pic".asPrompt(), picture = failedPicture()))

            // When
            val ids = (findPendingChallenges answer FindPendingChallenges.Query).map { it.id }.toSet()

            // Then
            assertEquals(setOf(pendingA.id, pendingB.id), ids)
        }
}
