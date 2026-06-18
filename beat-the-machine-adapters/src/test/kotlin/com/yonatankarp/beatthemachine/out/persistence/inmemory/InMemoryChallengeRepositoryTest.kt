package com.yonatankarp.beatthemachine.out.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.out.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.Lives
import com.yonatankarp.beatthemachine.domain.Prompt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InMemoryChallengeRepositoryTest {
    private val repo = InMemoryChallengeRepository()

    @Test
    fun `saves and finds by id`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        repo.save(c)
        assertEquals(c.id, repo.findById(c.id)?.id)
    }

    @Test
    fun `rejects a stale version`() {
        val c = Challenge.start(Prompt("hello world"), Lives(3))
        repo.save(c) // stored version becomes 1
        // a second save carrying the original version 0 must conflict
        assertFailsWith<OptimisticLockConflict> { repo.save(c) }
    }
}
