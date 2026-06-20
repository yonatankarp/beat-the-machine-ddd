package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ForfeitChallengeUseCaseTest {
    private val store = FakeChallengeStore()

    private suspend fun seed(): Challenge = store(Challenge.start(Prompt("hello world"), Lives(3)))

    @Test
    fun `forfeit loads the challenge, sets LOST, and persists it`() =
        runTest {
            // Given
            val c = seed()
            val forfeitChallenge = ForfeitChallengeUseCase(store, store)

            // When
            val result = forfeitChallenge(c.id)

            // Then
            assertEquals(ChallengeStatus.LOST, result.status)
            assertEquals(ChallengeStatus.LOST, store(c.id)?.status)
        }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() =
        runTest {
            // Given
            val forfeitChallenge = ForfeitChallengeUseCase(store, store)
            val unknownId = ChallengeId.new()

            // When / Then
            assertFailsWith<ChallengeNotFound> {
                forfeitChallenge(unknownId)
            }
        }
}
