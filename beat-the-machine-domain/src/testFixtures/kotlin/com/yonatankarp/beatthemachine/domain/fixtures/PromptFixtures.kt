package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.riddle.Prompt

object PromptFixtures {
    object Composed {
        fun goldenSunset() =
            Prompt(
                listOf(
                    WordFixtures.Colors.golden(),
                    WordFixtures.Nature.sunset(),
                ),
            )

        fun mountainLandscape() =
            Prompt(
                listOf(
                    WordFixtures.Nature.mountain(),
                    WordFixtures.Art.landscape(),
                ),
            )

        fun abstractArt() =
            Prompt(
                listOf(
                    WordFixtures.Art.abstract(),
                    WordFixtures.Art.geometric(),
                    WordFixtures.Activities.painting(),
                ),
            )

        fun naturePath() =
            Prompt(
                listOf(
                    WordFixtures.Nature.forest(),
                    WordFixtures.Objects.pathway(),
                    WordFixtures.Colors.emerald(),
                ),
            )
    }

    object Photography {
        fun goldenSunsetBeach() = Prompt.of("golden", "sunset", "over", "peaceful", "beach")

        fun mistyMountainPeak() = Prompt.of("misty", "mountain", "peak", "at", "dawn")

        fun autumnForestPath() = Prompt.of("autumn", "forest", "path", "covered", "in", "leaves")

        fun crystalClearLake() = Prompt.of("crystal", "clear", "lake", "reflecting", "sky")

        fun starryNightSky() = Prompt.of("starry", "night", "sky", "above", "desert")

        fun vintageCarCountryside() = Prompt.of("vintage", "car", "on", "countryside", "road")

        fun lighthouseStormyCoast() = Prompt.of("lighthouse", "on", "stormy", "coastal", "cliff")

        fun cherryBlossomsSpring() = Prompt.of("cherry", "blossoms", "in", "japanese", "garden")
    }

    object Art {
        fun abstractGeometricShapes() = Prompt.of("abstract", "geometric", "shapes", "in", "vibrant", "colors")

        fun vintagePortraitPainting() = Prompt.of("vintage", "portrait", "painting", "of", "elegant", "woman")

        fun minimalistStillLife() = Prompt.of("minimalist", "still", "life", "with", "ceramic", "vase")

        fun impressionistLandscape() = Prompt.of("impressionist", "landscape", "with", "flowing", "water")

        fun surrealDreamscape() = Prompt.of("surreal", "dreamscape", "with", "floating", "objects")

        fun modernSculpture() = Prompt.of("modern", "sculpture", "in", "urban", "plaza")

        fun watercolorFlowers() = Prompt.of("watercolor", "flowers", "in", "morning", "light")

        fun oilPaintingCityscape() = Prompt.of("oil", "painting", "of", "bustling", "cityscape")
    }

    object Activities {
        fun chefPreparingPasta() = Prompt.of("chef", "preparing", "fresh", "pasta", "in", "kitchen")

        fun runnerCrossingBridge() = Prompt.of("runner", "crossing", "stone", "bridge", "at", "sunrise")

        fun artistPaintingCanvas() = Prompt.of("artist", "painting", "landscape", "on", "canvas")

        fun dancerPerformingStage() = Prompt.of("dancer", "performing", "gracefully", "on", "stage")

        fun musicianPlayingPiano() = Prompt.of("musician", "playing", "piano", "in", "concert", "hall")

        fun gardenerTendingFlowers() = Prompt.of("gardener", "tending", "colorful", "flower", "garden")

        fun writerCafe() = Prompt.of("writer", "working", "in", "cozy", "café")

        fun photographerCaptureWildlife() = Prompt.of("photographer", "capturing", "wildlife", "in", "nature")
    }

    object Simple {
        fun helloWorld() = Prompt.of("hello", "world")

        fun sunsetBeach() = Prompt.of("sunset", "beach")

        fun mountainLandscape() = Prompt.of("mountain", "landscape")

        fun beautifulNature() = Prompt.of("beautiful", "nature")

        fun testExample() = Prompt.of("test", "example")
    }

    object Complex {
        fun evaluationScenario() = Prompt.of("I", "love", "my", "creative", "work")

        fun mixedPositions() = Prompt.of("beautiful", "sunset", "over", "mountains", "and", "valleys")

        fun longPrompt() = Prompt.of("magnificent", "ancient", "castle", "surrounded", "by", "enchanted", "forest", "under", "moonlight")

        fun shortPrompt() = Prompt.of("cat", "sleeping")

        fun singleWord() = Prompt.of("serenity")
    }

    object EdgeCases {
        fun duplicateWords() = Prompt.of("blue", "sky", "blue", "ocean")

        fun allSameWord() = Prompt.of("test", "test", "test")

        fun alternatingPattern() = Prompt.of("red", "blue", "red", "blue", "red")
    }

    fun randomPhotography() =
        listOf(
            Photography.goldenSunsetBeach(),
            Photography.mistyMountainPeak(),
            Photography.autumnForestPath(),
            Photography.crystalClearLake(),
            Photography.starryNightSky(),
            Photography.vintageCarCountryside(),
            Photography.lighthouseStormyCoast(),
            Photography.cherryBlossomsSpring(),
        ).random()

    fun randomArt() =
        listOf(
            Art.abstractGeometricShapes(),
            Art.vintagePortraitPainting(),
            Art.minimalistStillLife(),
            Art.impressionistLandscape(),
            Art.surrealDreamscape(),
            Art.modernSculpture(),
            Art.watercolorFlowers(),
            Art.oilPaintingCityscape(),
        ).random()

    fun randomActivity() =
        listOf(
            Activities.chefPreparingPasta(),
            Activities.runnerCrossingBridge(),
            Activities.artistPaintingCanvas(),
            Activities.dancerPerformingStage(),
            Activities.musicianPlayingPiano(),
            Activities.gardenerTendingFlowers(),
            Activities.writerCafe(),
            Activities.photographerCaptureWildlife(),
        ).random()
}
