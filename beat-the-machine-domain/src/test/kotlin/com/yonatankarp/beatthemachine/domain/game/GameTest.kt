package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.fixtures.GameFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.GuessFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.RiddleFixtures
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class GameTest {
    @Nested
    inner class Creation {
        @Test
        fun `should generate unique game id when started`() {
            // Given / When
            val game = Game.start()

            // Then
            game.id shouldNotBe null
        }

        @Test
        fun `should start game with collection of riddles`() {
            // Given
            val riddles = RiddleFixtures.photographyCollection()

            // When
            val game = Game.start(riddles)

            // Then
            game.riddles shouldBe riddles
        }

        @Test
        fun `should start game with riddles passed as varargs`() {
            // Given
            val riddle1 = RiddleFixtures.Photography.goldenSunsetBeach()
            val riddle2 = RiddleFixtures.Art.abstractGeometricShapes()

            // When
            val game = Game.start(riddle1, riddle2)

            // Then
            game.riddles shouldBe listOf(riddle1, riddle2)
        }

        @Test
        fun `should start game with current riddle index at 0`() {
            // Given
            val game = GameFixtures.SingleRiddle.photographyGame()

            // When & Then
            game.currentRiddleIndex shouldBe 0
        }

        @Test
        fun `should start game with empty attempt history`() {
            // Given
            val game = GameFixtures.SingleRiddle.simpleGame()

            // When & Then
            game.attemptHistory.isEmpty shouldBe true
        }
    }

    @Nested
    inner class GameplayFlow {
        @Test
        fun `should add guess to attempt history when submitting guess`() {
            // Given
            val game = GameFixtures.SingleRiddle.simpleGame()
            val guess = GuessFixtures.Perfect.helloWorld()

            // When
            val updatedGame = game.submitGuess(guess)

            // Then
            updatedGame.attemptHistory.isEmpty shouldBe false
        }

        @Test
        fun `should increment current riddle index when moving to next riddle`() {
            // Given
            val game = GameFixtures.MultipleRiddles.twoRiddles()

            // When
            val updatedGame = game.nextRiddle()

            // Then
            updatedGame.currentRiddleIndex shouldBe 1
        }

        @Test
        fun `should return current riddle based on current riddle index`() {
            // Given
            val riddles = RiddleFixtures.photographyCollection()
            val game = Game.start(riddles)

            // When
            val currentRiddle = game.currentRiddle

            // Then
            currentRiddle shouldBe riddles.first()
        }
    }

    @Nested
    inner class GameCompletion {
        @Test
        fun `should return true for isComplete when current riddle index reaches end of riddles`() {
            // Given
            val game = GameFixtures.SingleRiddle.simpleGame()

            // When
            val updatedGame = game.nextRiddle()

            // Then
            updatedGame.isComplete shouldBe true
        }

        @Test
        fun `should return false for isComplete when not at end of riddles`() {
            // Given
            val game = GameFixtures.MultipleRiddles.twoRiddles()

            // When & Then
            game.isComplete shouldBe false
        }

        @Test
        fun `should handle completion with multiple riddles`() {
            // Given
            val game = GameFixtures.MultipleRiddles.threeRiddles()

            // When
            val afterFirst = game.nextRiddle()
            val afterSecond = afterFirst.nextRiddle()
            val completed = afterSecond.nextRiddle()

            // Then
            completed.isComplete shouldBe true
            completed.currentRiddleIndex shouldBe 3
        }
    }

    @Nested
    inner class ComplexScenarios {
        @Test
        fun `should handle game with multiple attempts and riddle progression`() {
            // Given
            val game = GameFixtures.MultipleRiddles.twoRiddles()

            // When
            val withGuess = game.submitGuess(GuessFixtures.Partial.wrongOrder())
            val nextRiddle = withGuess.nextRiddle()
            val finalGuess = nextRiddle.submitGuess(GuessFixtures.Perfect.goldenSunsetBeach())

            // Then
            finalGuess.currentRiddleIndex shouldBe 1
            finalGuess.attemptHistory.isEmpty shouldBe false
        }
    }

    @Nested
    inner class GameState {
        @Test
        fun `should start game with state IN_PROGRESS`() {
            // Given
            val game = GameFixtures.SingleRiddle.photographyGame()

            // When & Then
            game.state shouldBe Game.State.IN_PROGRESS
        }

        @Test
        fun `should maintain IN_PROGRESS state during gameplay`() {
            // Given
            val game = GameFixtures.MultipleRiddles.mixedThemes()

            // When
            val withGuess = game.submitGuess(GuessFixtures.randomGuess())
            val nextRiddle = withGuess.nextRiddle()

            // Then
            nextRiddle.state shouldBe Game.State.IN_PROGRESS
        }
    }

    @Nested
    inner class IdTest {
        @Test
        fun `should create game id with uuid`() {
            // Given
            val uuid = UUID.randomUUID()

            // When
            val gameId = Game.Id(uuid)

            // Then
            gameId.value shouldBe uuid
        }

        @Test
        fun `should generate non-null uuid when creating new id`() {
            // Given / When
            val gameId = Game.Id.new()

            // Then
            gameId.value shouldNotBe null
            gameId.value.toString().length shouldBe 36
        }

        @Test
        fun `should generate valid UUID format when creating new id`() {
            // Given / When
            val gameId = Game.Id.new()

            // Then
            val uuid = gameId.value
            uuid shouldNotBe null
            uuid.version() shouldBe 4
            uuid.variant() shouldBe 2
            uuid.toString().matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) shouldBe true
        }

        @Test
        fun `should create unique ids on multiple calls`() {
            // Given / When
            val id1 = Game.Id.new()
            val id2 = Game.Id.new()
            val id3 = Game.Id.new()

            // Then
            id1.value shouldNotBe null
            id2.value shouldNotBe null
            id3.value shouldNotBe null
            id1.value shouldNotBe id2.value
            id1.value shouldNotBe id3.value
            id2.value shouldNotBe id3.value
        }
    }
}
