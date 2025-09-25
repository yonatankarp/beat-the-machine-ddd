package com.yonatankarp.beatthemachine.domain.riddle

import com.yonatankarp.beatthemachine.domain.fixtures.GuessFixtures
import com.yonatankarp.beatthemachine.domain.fixtures.RiddleFixtures
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RiddleTest {
    @Nested
    inner class Evaluation {
        @Test
        fun `should create riddle and evaluate guess returning result`() {
            // Given
            val riddle = RiddleFixtures.Simple.sunsetBeach()
            val guess = GuessFixtures.Perfect.sunsetBeach()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.feedbacks.size shouldBe 2
            result.isAllCorrect shouldBe true
        }

        @Test
        fun `should evaluate guess with mixed feedback statuses`() {
            // Given
            val riddle = RiddleFixtures.Evaluation.complexEvaluation()
            val guess = GuessFixtures.Evaluation.mixedPositions()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.feedbacks.size shouldBe 5
            result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
            result[1] shouldBe WordFeedback.Status.WRONG_POSITION
            result[2] shouldBe WordFeedback.Status.CORRECT_POSITION
            result[3] shouldBe WordFeedback.Status.WRONG_WORD
            result[4] shouldBe WordFeedback.Status.WRONG_WORD
        }

        @Test
        fun `should return true when all words are in correct position`() {
            // Given
            val riddle = RiddleFixtures.Evaluation.complexEvaluation()
            val guess = GuessFixtures.Evaluation.allCorrectPositions()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.isAllCorrect shouldBe true
        }

        @Test
        fun `should access feedback status by index using operator`() {
            // Given
            val riddle = RiddleFixtures.Evaluation.complexEvaluation()
            val guess = GuessFixtures.Evaluation.mixedPositions()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        }
    }

    @Nested
    inner class Properties {
        @Test
        fun `should expose prompt through property`() {
            // Given
            val riddle = RiddleFixtures.Simple.helloWorld()

            // When & Then
            riddle.prompt shouldBe riddle.prompt
        }

        @Test
        fun `should expose imageUrl through property`() {
            // Given
            val riddle = RiddleFixtures.Photography.goldenSunsetBeach()

            // When & Then
            riddle.imageUrl shouldBe riddle.imageUrl
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should return wrong word when guess index exceeds prompt size`() {
            // Given
            val riddle = RiddleFixtures.Simple.sunsetBeach()
            val guess = GuessFixtures.EdgeCases.tooLong()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result[2] shouldBe WordFeedback.Status.WRONG_WORD
        }

        @Test
        fun `should handle single word riddle evaluation`() {
            // Given
            val riddle = RiddleFixtures.Evaluation.singleWord()
            val guess = GuessFixtures.EdgeCases.singleWord()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.feedbacks.size shouldBe 1
            result[0] shouldBe WordFeedback.Status.CORRECT_POSITION
        }

        @Test
        fun `should handle riddle with duplicate words`() {
            // Given
            val riddle = RiddleFixtures.EdgeCases.duplicateWords()
            val guess = GuessFixtures.EdgeCases.duplicateWords()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.feedbacks.size shouldBe 4
        }
    }

    @Nested
    inner class ComplexScenarios {
        @Test
        fun `should evaluate photography themed riddle correctly`() {
            // Given
            val riddle = RiddleFixtures.Photography.goldenSunsetBeach()
            val guess = GuessFixtures.Perfect.goldenSunsetBeach()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.isAllCorrect shouldBe true
            result.feedbacks.size shouldBe 5
        }

        @Test
        fun `should evaluate art themed riddle with partial match`() {
            // Given
            val riddle = RiddleFixtures.Art.abstractGeometricShapes()
            val guess = GuessFixtures.Partial.someCorrectWords()

            // When
            val result = riddle.evaluate(guess)

            // Then
            result.isAllCorrect shouldBe false
            result.feedbacks.size shouldBe 5
        }
    }
}
