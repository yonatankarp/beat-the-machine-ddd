package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetChallengeUseCaseTest {
    private val store = FakeChallengeStore()
    private val getChallenge = GetChallengeUseCase(store)

    @Test
    fun `returns a challenge that exists`() =
        runTest {
            // Given
            val challenge = store(mediumChallenge(prompt = "red fox".asPrompt()))

            // When
            val result = getChallenge(challenge.id)

            // Then
            assertEquals(challenge, result)
        }

    @Test
    fun `throws ChallengeNotFound for an unknown id`() =
        runTest {
            // Given
            val unknownId = aChallengeId()

            // When / Then
            assertFailsWith<ChallengeNotFound> { getChallenge(unknownId) }
        }
}
