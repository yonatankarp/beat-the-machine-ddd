package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InMemoryFindPendingChallengesTest {
    private val store = InMemoryChallengeStore()
    private val storeChallenge = InMemoryStoreChallenge(store)
    private val findPendingChallenges = InMemoryFindPendingChallenges(store)

    @Test
    fun `returns only challenges whose picture is pending`() {
        val pendingA = storeChallenge(Challenge.start(Prompt("hello world"), Lives(3)))
        val pendingB = storeChallenge(Challenge.start(Prompt("red fox"), Lives(3)))
        storeChallenge(Challenge.start(Prompt("foo bar"), Lives(3)).withPicture(Picture.Ready("http://img/1.png")))

        val ids = findPendingChallenges().map { it.id }.toSet()
        assertEquals(setOf(pendingA.id, pendingB.id), ids)
    }
}
