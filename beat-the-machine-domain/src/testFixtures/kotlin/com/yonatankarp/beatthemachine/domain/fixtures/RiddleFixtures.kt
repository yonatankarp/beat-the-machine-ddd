package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.riddle.Riddle

object RiddleFixtures {
    object Photography {
        fun goldenSunsetBeach() =
            Riddle(
                prompt = PromptFixtures.Photography.goldenSunsetBeach(),
                imageUrl = ImageUrlFixtures.Photography.goldenSunsetBeach(),
            )

        fun mistyMountainPeak() =
            Riddle(
                prompt = PromptFixtures.Photography.mistyMountainPeak(),
                imageUrl = ImageUrlFixtures.Photography.mistyMountainPeak(),
            )

        fun autumnForestPath() =
            Riddle(
                prompt = PromptFixtures.Photography.autumnForestPath(),
                imageUrl = ImageUrlFixtures.Photography.autumnForestPath(),
            )

        fun crystalClearLake() =
            Riddle(
                prompt = PromptFixtures.Photography.crystalClearLake(),
                imageUrl = ImageUrlFixtures.Photography.crystalClearLake(),
            )

        fun starryNightSky() =
            Riddle(
                prompt = PromptFixtures.Photography.starryNightSky(),
                imageUrl = ImageUrlFixtures.Photography.starryNightSky(),
            )
    }

    object Art {
        fun abstractGeometricShapes() =
            Riddle(
                prompt = PromptFixtures.Art.abstractGeometricShapes(),
                imageUrl = ImageUrlFixtures.Art.abstractGeometricShapes(),
            )

        fun vintagePortraitPainting() =
            Riddle(
                prompt = PromptFixtures.Art.vintagePortraitPainting(),
                imageUrl = ImageUrlFixtures.Art.vintagePortraitPainting(),
            )

        fun minimalistStillLife() =
            Riddle(
                prompt = PromptFixtures.Art.minimalistStillLife(),
                imageUrl = ImageUrlFixtures.Art.minimalistStillLife(),
            )

        fun impressionistLandscape() =
            Riddle(
                prompt = PromptFixtures.Art.impressionistLandscape(),
                imageUrl = ImageUrlFixtures.Art.impressionistLandscape(),
            )

        fun surrealDreamscape() =
            Riddle(
                prompt = PromptFixtures.Art.surrealDreamscape(),
                imageUrl = ImageUrlFixtures.Art.surrealDreamscape(),
            )
    }

    object Activities {
        fun chefPreparingPasta() =
            Riddle(
                prompt = PromptFixtures.Activities.chefPreparingPasta(),
                imageUrl = ImageUrlFixtures.Activities.chefPreparingPasta(),
            )

        fun runnerCrossingBridge() =
            Riddle(
                prompt = PromptFixtures.Activities.runnerCrossingBridge(),
                imageUrl = ImageUrlFixtures.Activities.runnerCrossingBridge(),
            )

        fun artistPaintingCanvas() =
            Riddle(
                prompt = PromptFixtures.Activities.artistPaintingCanvas(),
                imageUrl = ImageUrlFixtures.Activities.artistPaintingCanvas(),
            )

        fun dancerPerformingStage() =
            Riddle(
                prompt = PromptFixtures.Activities.dancerPerformingStage(),
                imageUrl = ImageUrlFixtures.Activities.dancerPerformingStage(),
            )
    }

    object Simple {
        fun helloWorld() =
            Riddle(
                prompt = PromptFixtures.Simple.helloWorld(),
                imageUrl = ImageUrlFixtures.Simple.defaultImage(),
            )

        fun sunsetBeach() =
            Riddle(
                prompt = PromptFixtures.Simple.sunsetBeach(),
                imageUrl = ImageUrlFixtures.Simple.testImage(),
            )

        fun mountainLandscape() =
            Riddle(
                prompt = PromptFixtures.Simple.mountainLandscape(),
                imageUrl = ImageUrlFixtures.Simple.sampleImage(),
            )

        fun testRiddle() =
            Riddle(
                prompt = PromptFixtures.Simple.testExample(),
                imageUrl = ImageUrlFixtures.Simple.placeholderImage(),
            )
    }

    object Evaluation {
        fun complexEvaluation() =
            Riddle(
                prompt = PromptFixtures.Complex.evaluationScenario(),
                imageUrl = ImageUrlFixtures.Simple.defaultImage(),
            )

        fun mixedPositions() =
            Riddle(
                prompt = PromptFixtures.Complex.mixedPositions(),
                imageUrl = ImageUrlFixtures.Simple.testImage(),
            )

        fun longPrompt() =
            Riddle(
                prompt = PromptFixtures.Complex.longPrompt(),
                imageUrl = ImageUrlFixtures.Simple.sampleImage(),
            )

        fun shortPrompt() =
            Riddle(
                prompt = PromptFixtures.Complex.shortPrompt(),
                imageUrl = ImageUrlFixtures.Simple.placeholderImage(),
            )

        fun singleWord() =
            Riddle(
                prompt = PromptFixtures.Complex.singleWord(),
                imageUrl = ImageUrlFixtures.Simple.defaultImage(),
            )
    }

    object EdgeCases {
        fun duplicateWords() =
            Riddle(
                prompt = PromptFixtures.EdgeCases.duplicateWords(),
                imageUrl = ImageUrlFixtures.Simple.testImage(),
            )

        fun allSameWord() =
            Riddle(
                prompt = PromptFixtures.EdgeCases.allSameWord(),
                imageUrl = ImageUrlFixtures.Simple.sampleImage(),
            )

        fun alternatingPattern() =
            Riddle(
                prompt = PromptFixtures.EdgeCases.alternatingPattern(),
                imageUrl = ImageUrlFixtures.Simple.placeholderImage(),
            )
    }

    fun randomPhotography() =
        listOf(
            Photography.goldenSunsetBeach(),
            Photography.mistyMountainPeak(),
            Photography.autumnForestPath(),
            Photography.crystalClearLake(),
            Photography.starryNightSky(),
        ).random()

    fun randomArt() =
        listOf(
            Art.abstractGeometricShapes(),
            Art.vintagePortraitPainting(),
            Art.minimalistStillLife(),
            Art.impressionistLandscape(),
            Art.surrealDreamscape(),
        ).random()

    fun randomActivity() =
        listOf(
            Activities.chefPreparingPasta(),
            Activities.runnerCrossingBridge(),
            Activities.artistPaintingCanvas(),
            Activities.dancerPerformingStage(),
        ).random()

    fun randomRiddle() =
        listOf(
            randomPhotography(),
            randomArt(),
            randomActivity(),
        ).random()

    fun collectionOfRiddles() =
        listOf(
            Photography.goldenSunsetBeach(),
            Art.abstractGeometricShapes(),
            Activities.chefPreparingPasta(),
            Simple.helloWorld(),
            Evaluation.complexEvaluation(),
        )

    fun photographyCollection() =
        listOf(
            Photography.goldenSunsetBeach(),
            Photography.mistyMountainPeak(),
            Photography.autumnForestPath(),
            Photography.crystalClearLake(),
        )

    fun artCollection() =
        listOf(
            Art.abstractGeometricShapes(),
            Art.vintagePortraitPainting(),
            Art.minimalistStillLife(),
            Art.impressionistLandscape(),
        )
}
