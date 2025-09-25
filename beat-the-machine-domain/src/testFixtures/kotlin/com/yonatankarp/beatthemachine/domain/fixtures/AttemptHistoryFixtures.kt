package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.game.AttemptHistory

object AttemptHistoryFixtures {
    object Empty {
        fun noAttempts() = AttemptHistory.empty()
    }

    object SingleAttempt {
        fun simpleGuess() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())

        fun photographyGuess() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.sunsetBeach())

        fun artGuess() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Simple.hello())
    }

    object MultipleAttempts {
        fun twoGuesses() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.sunsetBeach())

        fun threeGuesses() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.sunsetBeach())
                .addGuess(GuessFixtures.Simple.hello())

        fun progressiveGuesses() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.EdgeCases.singleWord())
                .addGuess(GuessFixtures.Simple.hello())
                .addGuess(GuessFixtures.EdgeCases.tooLong())
    }

    object Scenarios {
        fun gameInProgress() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.sunsetBeach())
                .addGuess(GuessFixtures.Perfect.mountainLandscape())

        fun nearMaxAttempts() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.sunsetBeach())
                .addGuess(GuessFixtures.Simple.hello())
                .addGuess(GuessFixtures.Simple.world())

        fun fullGameHistory() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.sunsetBeach())
                .addGuess(GuessFixtures.Simple.hello())
                .addGuess(GuessFixtures.Simple.world())
                .addGuess(GuessFixtures.EdgeCases.tooLong())
    }

    object EdgeCases {
        fun duplicateGuesses() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.helloWorld())
                .addGuess(GuessFixtures.Perfect.helloWorld())

        fun mixedComplexity() =
            AttemptHistory
                .empty()
                .addGuess(GuessFixtures.EdgeCases.singleWord())
                .addGuess(GuessFixtures.EdgeCases.tooLong())
                .addGuess(GuessFixtures.Simple.hello())
    }

    fun randomSingleAttempt() =
        listOf(
            SingleAttempt.simpleGuess(),
            SingleAttempt.photographyGuess(),
            SingleAttempt.artGuess(),
        ).random()

    fun randomMultipleAttempts() =
        listOf(
            MultipleAttempts.twoGuesses(),
            MultipleAttempts.threeGuesses(),
            MultipleAttempts.progressiveGuesses(),
        ).random()

    fun randomScenario() =
        listOf(
            Scenarios.gameInProgress(),
            Scenarios.nearMaxAttempts(),
            Scenarios.fullGameHistory(),
        ).random()
}
