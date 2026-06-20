package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeStatus
import com.yonatankarp.beatthemachine.test.dsl.aChallengeId
import com.yonatankarp.beatthemachine.test.fixtures.Challenges.mediumChallenge
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ForfeitChallengeUseCaseTest {
    private val store = FakeChallengeStore()

    private suspend fun seed(): Challenge = store(mediumChallenge())

    @Test
    fun `forfeit loads the challenge, sets LOST, and persists it`() =
        runTest {
            // Given
            val c = seed()
            val forfeitChallenge = ForfeitChallengeUseCase(store, store)

            // When
            val result = forfeitChallenge handle ForfeitChallenge.Command(c.id)

            // Then
            assertEquals(ChallengeStatus.LOST, result.status)
            assertEquals(ChallengeStatus.LOST, (store answer FindChallengeById.Query(c.id))?.status)
        }

    @Test
    fun `an unknown challenge throws ChallengeNotFound`() =
        runTest {
            // Given
            val forfeitChallenge = ForfeitChallengeUseCase(store, store)
            val unknownId = aChallengeId()

            // When / Then
            assertFailsWith<ChallengeNotFound> {
                forfeitChallenge handle ForfeitChallenge.Command(unknownId)
            }
        }
}
