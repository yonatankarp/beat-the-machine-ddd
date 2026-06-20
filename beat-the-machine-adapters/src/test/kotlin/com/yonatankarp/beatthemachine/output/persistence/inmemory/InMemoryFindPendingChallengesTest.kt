package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
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
            val pendingA = storeChallenge(Challenge.start(Prompt("hello world"), Lives(3)))
            val pendingB = storeChallenge(Challenge.start(Prompt("red fox"), Lives(3)))
            storeChallenge(Challenge.start(Prompt("foo bar"), Lives(3)).withPicture(Picture.Ready("http://img/1.png")))

            // When
            val ids = findPendingChallenges().map { it.id }.toSet()

            // Then
            assertEquals(setOf(pendingA.id, pendingB.id), ids)
        }
}
