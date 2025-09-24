package com.yonatankarp.beatthemachine.domain.game

import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.ImageUrl
import com.yonatankarp.beatthemachine.domain.riddle.Prompt
import com.yonatankarp.beatthemachine.domain.riddle.Riddle
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class GameTest {
    @Nested
    inner class GameTest {
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
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                    Riddle(
                        prompt = Prompt.of("mountain", "landscape"),
                        imageUrl = ImageUrl("https://example.com/image2.jpg"),
                    ),
                )

            // When
            val game = Game.start(riddles)

            // Then
            game.riddles shouldBe riddles
        }

        @Test
        fun `should start game with current riddle index at 0`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                )

            // When
            val game = Game.start(riddles)

            // Then
            game.currentRiddleIndex shouldBe 0
        }

        @Test
        fun `should start game with empty attempt history`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                )

            // When
            val game = Game.start(riddles)

            // Then
            game.attemptHistory.isEmpty shouldBe true
        }

        @Test
        fun `should add guess to attempt history when submitting guess`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                )
            val game = Game.start(riddles)
            val guess = Guess.of("sunset", "beach")

            // When
            val updatedGame = game.submitGuess(guess)

            // Then
            updatedGame.attemptHistory.isEmpty shouldBe false
        }

        @Test
        fun `should increment current riddle index when moving to next riddle`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                    Riddle(
                        prompt = Prompt.of("mountain", "landscape"),
                        imageUrl = ImageUrl("https://example.com/image2.jpg"),
                    ),
                )
            val game = Game.start(riddles)

            // When
            val updatedGame = game.nextRiddle()

            // Then
            updatedGame.currentRiddleIndex shouldBe 1
        }

        @Test
        fun `should return true for isComplete when current riddle index reaches end of riddles`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                )
            val game = Game.start(riddles)

            // When
            val updatedGame = game.nextRiddle()

            // Then
            updatedGame.isComplete shouldBe true
        }

        @Test
        fun `should return current riddle based on current riddle index`() {
            // Given
            val firstRiddle =
                Riddle(
                    prompt = Prompt.of("sunset", "beach"),
                    imageUrl = ImageUrl("https://example.com/image1.jpg"),
                )
            val secondRiddle =
                Riddle(
                    prompt = Prompt.of("mountain", "landscape"),
                    imageUrl = ImageUrl("https://example.com/image2.jpg"),
                )
            val riddles = listOf(firstRiddle, secondRiddle)
            val game = Game.start(riddles)

            // When
            val currentRiddle = game.currentRiddle

            // Then
            currentRiddle shouldBe firstRiddle
        }

        @Test
        fun `should return false for isComplete when not at end of riddles`() {
            // Given
            val riddle1 =
                Riddle(Prompt.of("test"), ImageUrl("https://example.com/1.jpg"))
            val riddle2 =
                Riddle(
                    Prompt.of("test2"),
                    ImageUrl("https://example.com/2.jpg"),
                )
            val game = Game.start(listOf(riddle1, riddle2))

            // When & Then
            game.isComplete shouldBe false
        }
    }

    @Nested
    inner class StatusTest {
        @Test
        fun `should start game with state IN_PROGRESS`() {
            // Given
            val riddles =
                listOf(
                    Riddle(
                        prompt = Prompt.of("sunset", "beach"),
                        imageUrl = ImageUrl("https://example.com/image1.jpg"),
                    ),
                )

            // When
            val game = Game.start(riddles)

            // Then
            game.state shouldBe Game.State.IN_PROGRESS
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
    }
}
