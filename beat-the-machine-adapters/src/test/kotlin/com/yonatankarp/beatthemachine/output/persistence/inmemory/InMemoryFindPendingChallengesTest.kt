package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.lives
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import com.yonatankarp.beatthemachine.test.fixtures.Pictures.readyPicture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InMemoryFindPendingChallengesTest {
    @Test
    fun `returns only challenges whose picture is pending`() =
        runTest {
            // Given
            val store = InMemoryChallengeStore()
            val storeChallenge = InMemoryStoreChallenge(store)
            val findPendingChallenges = InMemoryFindPendingChallenges(store)
            val pendingA = storeChallenge(mediumChallenge(lives = 3.lives()))
            val pendingB = storeChallenge(mediumChallenge(lives = 3.lives(), prompt = "red fox".asPrompt()))
            storeChallenge(mediumChallenge(lives = 3.lives(), prompt = "foo bar".asPrompt()).withPicture(readyPicture("http://img/1.png")))

            // When
            val ids = findPendingChallenges().map { it.id }.toSet()

            // Then
            assertEquals(setOf(pendingA.id, pendingB.id), ids)
        }
}
