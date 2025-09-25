package com.yonatankarp.beatthemachine.application.usecases

import com.yonatankarp.beatthemachine.application.ports.output.FindAvailableRiddles
import com.yonatankarp.beatthemachine.domain.fixtures.RiddleFixtures
import com.yonatankarp.beatthemachine.domain.riddle.Riddle
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class StartGameUseCaseTest {
    @Test
    fun `should start game with available riddles from repository`() {
        // Given
        val availableRiddles = RiddleFixtures.collectionOfRiddles()
        val startGameUseCase = createStartGameUseCase(availableRiddles)

        // When
        val game = startGameUseCase()

        // Then
        game shouldNotBe null
        game.riddles shouldContainExactly availableRiddles
    }

    @Test
    fun `should start game with photography themed riddles`() {
        // Given
        val photographyRiddles = RiddleFixtures.photographyCollection()
        val startGameUseCase = createStartGameUseCase(photographyRiddles)

        // When
        val game = startGameUseCase()

        // Then
        game shouldNotBe null
        game.riddles shouldContainExactly photographyRiddles
        game.currentRiddleIndex shouldBe 0
        game.attemptHistory.isEmpty shouldBe true
    }

    @Test
    fun `should start game with art themed riddles`() {
        // Given
        val artRiddles = RiddleFixtures.artCollection()
        val startGameUseCase = createStartGameUseCase(artRiddles)

        // When
        val game = startGameUseCase()

        // Then
        game shouldNotBe null
        game.riddles shouldContainExactly artRiddles
        game.state shouldBe com.yonatankarp.beatthemachine.domain.game.Game.State.IN_PROGRESS
    }

    private fun createStartGameUseCase(availableRiddles: List<Riddle>): StartGame {
        val findAvailableRiddles = FindAvailableRiddles { availableRiddles }
        return StartGame(findAvailableRiddles)
    }
}
