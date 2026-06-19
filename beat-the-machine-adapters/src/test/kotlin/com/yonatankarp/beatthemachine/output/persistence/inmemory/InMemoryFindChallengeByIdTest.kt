package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryFindChallengeByIdTest {
    private val store = InMemoryChallengeStore()
    private val storeChallenge = InMemoryStoreChallenge(store)
    private val findChallengeById = InMemoryFindChallengeById(store)

    @Test
    fun `finds a stored challenge`() =
        runTest {
            val saved = storeChallenge(Challenge.start(Prompt("hello world"), Lives(3)))
            assertEquals(saved.id, findChallengeById(saved.id)?.id)
        }

    @Test
    fun `returns null for an unknown id`() =
        runTest {
            assertNull(findChallengeById(ChallengeId.new()))
        }
}
