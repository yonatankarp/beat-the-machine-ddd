package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.game.Game

object GameFixtures {
    object SingleRiddle {
        fun photographyGame() = Game.start(RiddleFixtures.Photography.goldenSunsetBeach())

        fun artGame() = Game.start(RiddleFixtures.Art.abstractGeometricShapes())

        fun activityGame() = Game.start(RiddleFixtures.Activities.chefPreparingPasta())

        fun simpleGame() = Game.start(RiddleFixtures.Simple.helloWorld())

        fun evaluationGame() = Game.start(RiddleFixtures.Evaluation.complexEvaluation())

        fun edgeCaseGame() = Game.start(RiddleFixtures.EdgeCases.duplicateWords())
    }

    object MultipleRiddles {
        fun photographyCollection() = Game.start(RiddleFixtures.photographyCollection())

        fun artCollection() = Game.start(RiddleFixtures.artCollection())

        fun mixedThemes() = Game.start(RiddleFixtures.collectionOfRiddles())

        fun twoRiddles() =
            Game.start(
                RiddleFixtures.Photography.goldenSunsetBeach(),
                RiddleFixtures.Art.abstractGeometricShapes(),
            )

        fun threeRiddles() =
            Game.start(
                RiddleFixtures.Photography.mistyMountainPeak(),
                RiddleFixtures.Activities.runnerCrossingBridge(),
                RiddleFixtures.Art.minimalistStillLife(),
            )

        fun longGame() =
            Game.start(
                RiddleFixtures.Photography.goldenSunsetBeach(),
                RiddleFixtures.Photography.mistyMountainPeak(),
                RiddleFixtures.Art.abstractGeometricShapes(),
                RiddleFixtures.Art.vintagePortraitPainting(),
                RiddleFixtures.Activities.chefPreparingPasta(),
                RiddleFixtures.Activities.runnerCrossingBridge(),
                RiddleFixtures.Simple.helloWorld(),
                RiddleFixtures.Evaluation.complexEvaluation(),
            )
    }

    object WithAttemptHistory {
        fun gameWithSingleAttempt() =
            Game
                .start(RiddleFixtures.Simple.helloWorld())
                .copy(attemptHistory = AttemptHistoryFixtures.SingleAttempt.simpleGuess())

        fun gameWithMultipleAttempts() =
            Game
                .start(RiddleFixtures.Evaluation.complexEvaluation())
                .copy(attemptHistory = AttemptHistoryFixtures.MultipleAttempts.threeGuesses())

        fun gameNearMaxAttempts() =
            Game
                .start(RiddleFixtures.Photography.goldenSunsetBeach())
                .copy(attemptHistory = AttemptHistoryFixtures.Scenarios.nearMaxAttempts())

        fun gameWithFullHistory() =
            Game
                .start(RiddleFixtures.Art.abstractGeometricShapes())
                .copy(attemptHistory = AttemptHistoryFixtures.Scenarios.fullGameHistory())
    }

    object GameStates {
        fun newGame() = Game.start(RiddleFixtures.collectionOfRiddles())

        fun gameWithAttempts(): Game {
            val game = Game.start(RiddleFixtures.Simple.helloWorld())
            return game.submitGuess(GuessFixtures.Perfect.helloWorld())
        }

        fun gameWithMultipleAttempts(): Game {
            val game = Game.start(RiddleFixtures.Evaluation.complexEvaluation())
            return game
                .submitGuess(GuessFixtures.Partial.mixedMatch())
                .submitGuess(GuessFixtures.Wrong.completelyWrong())
                .submitGuess(GuessFixtures.Evaluation.allCorrectPositions())
        }

        fun gameAtSecondRiddle(): Game {
            val game =
                Game.start(
                    RiddleFixtures.Simple.helloWorld(),
                    RiddleFixtures.Photography.goldenSunsetBeach(),
                )
            return game.nextRiddle()
        }

        fun completedGame(): Game {
            val game = Game.start(RiddleFixtures.Simple.helloWorld())
            return game.nextRiddle()
        }

        fun gameNearCompletion(): Game {
            val game =
                Game.start(
                    RiddleFixtures.Simple.helloWorld(),
                    RiddleFixtures.Simple.sunsetBeach(),
                )
            return game.nextRiddle()
        }
    }

    object EmptyStates {
        fun emptyGame() = Game.start()
    }

    fun defaultGame() = SingleRiddle.simpleGame()

    fun randomSingleRiddleGame() =
        listOf(
            SingleRiddle.photographyGame(),
            SingleRiddle.artGame(),
            SingleRiddle.activityGame(),
            SingleRiddle.simpleGame(),
            SingleRiddle.evaluationGame(),
        ).random()

    fun randomMultiRiddleGame() =
        listOf(
            MultipleRiddles.photographyCollection(),
            MultipleRiddles.artCollection(),
            MultipleRiddles.mixedThemes(),
            MultipleRiddles.twoRiddles(),
            MultipleRiddles.threeRiddles(),
        ).random()

    fun gameForTesting() =
        Game.start(
            RiddleFixtures.Simple.testRiddle(),
            RiddleFixtures.Evaluation.complexEvaluation(),
        )
}
