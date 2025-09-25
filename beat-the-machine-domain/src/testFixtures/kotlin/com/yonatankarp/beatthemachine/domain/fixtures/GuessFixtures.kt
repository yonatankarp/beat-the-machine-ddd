package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.riddle.Guess
import com.yonatankarp.beatthemachine.domain.riddle.Prompt

object GuessFixtures {
    object Perfect {
        fun goldenSunsetBeach() = Guess.of("golden", "sunset", "over", "peaceful", "beach")

        fun mistyMountainPeak() = Guess.of("misty", "mountain", "peak", "at", "dawn")

        fun helloWorld() = Guess.of("hello", "world")

        fun sunsetBeach() = Guess.of("sunset", "beach")

        fun mountainLandscape() = Guess.of("mountain", "landscape")
    }

    object Partial {
        fun someCorrectWords() = Guess.of("golden", "beach", "over", "calm", "water")

        fun wrongOrder() = Guess.of("beach", "sunset", "golden", "over", "peaceful")

        fun mixedMatch() = Guess.of("I", "work", "my", "life")

        fun partialOverlap() = Guess.of("beautiful", "ocean", "under", "starry", "sky")
    }

    object Wrong {
        fun completelyWrong() = Guess.of("cat", "dog", "house", "car", "tree")

        fun noMatch() = Guess.of("purple", "elephant", "flying", "through", "space")

        fun differentTheme() = Guess.of("robot", "machine", "technology", "future")
    }

    object EdgeCases {
        fun tooShort() = Guess.of("short")

        fun tooLong() =
            Guess.of(
                "very",
                "long",
                "guess",
                "with",
                "many",
                "words",
                "that",
                "exceeds",
                "prompt",
                "length",
            )

        fun duplicateWords() = Guess.of("blue", "ocean", "blue", "sky")

        fun singleWord() = Guess.of("serenity")

        fun emptyStrings() = Guess.of("", "valid", "word")
    }

    object Evaluation {
        fun allCorrectPositions() = Guess.of("I", "love", "my", "creative", "work")

        fun allWrongPositions() = Guess.of("work", "my", "creative", "love", "I")

        fun mixedPositions() = Guess.of("I", "work", "my", "life", "goals")

        fun someCorrectSomeWrong() = Guess.of("I", "enjoy", "my", "creative", "life")
    }

    object Simple {
        fun hello() = Guess.of("hello")

        fun world() = Guess.of("world")

        fun test() = Guess.of("test")

        fun example() = Guess.of("example")

        fun sample() = Guess.of("sample")
    }

    fun perfectMatchFor(prompt: Prompt): Guess {
        val words = prompt.words.map { word -> word.value }.toList()
        return Guess.of(*words.toTypedArray())
    }

    fun partialMatchFor(prompt: Prompt): Guess {
        val words =
            prompt.words
                .mapIndexed { index, word ->
                    if (index < prompt.words.size / 2) {
                        word.value
                    } else {
                        "different$index"
                    }
                }.toList()
        return Guess.of(*words.toTypedArray())
    }

    fun wrongOrderFor(prompt: Prompt): Guess {
        val words = prompt.words.map { it.value }.reversed()
        return Guess.of(*words.toTypedArray())
    }

    fun randomGuess() =
        listOf(
            Perfect.goldenSunsetBeach(),
            Perfect.mistyMountainPeak(),
            Partial.someCorrectWords(),
            Partial.wrongOrder(),
            Wrong.completelyWrong(),
            Wrong.noMatch(),
        ).random()
}
