package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
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
            val challenge = store(Challenge.start(Prompt("red fox"), Lives(6)))

            // When
            val result = getChallenge(challenge.id)

            // Then
            assertEquals(challenge, result)
        }

    @Test
    fun `throws ChallengeNotFound for an unknown id`() =
        runTest {
            // Given
            val unknownId = ChallengeId.new()

            // When / Then
            assertFailsWith<ChallengeNotFound> { getChallenge(unknownId) }
        }
}
