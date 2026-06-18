package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InMemoryStoreChallengeTest {
    private val store = InMemoryChallengeStore()
    private val storeChallenge = InMemoryStoreChallenge(store)

    @Test
    fun `stores and bumps the version`() {
        val saved = storeChallenge(Challenge.start(Prompt("hello world"), Lives(3)))
        assertEquals(1L, saved.version)
    }

    @Test
    fun `rejects a stale version`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        storeChallenge(c) // stored version becomes 1
        // a second store carrying the original version 0 must conflict
        assertFailsWith<OptimisticLockConflict> { storeChallenge(c) }
    }
}
