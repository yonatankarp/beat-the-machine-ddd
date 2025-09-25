package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.riddle.ImageUrl

object ImageUrlFixtures {
    object Photography {
        fun goldenSunsetBeach() = ImageUrl("https://images.example.com/photography/golden-sunset-beach.jpg")

        fun mistyMountainPeak() = ImageUrl("https://images.example.com/photography/misty-mountain-peak.jpg")

        fun autumnForestPath() = ImageUrl("https://images.example.com/photography/autumn-forest-path.jpg")

        fun crystalClearLake() = ImageUrl("https://images.example.com/photography/crystal-clear-lake.jpg")

        fun starryNightSky() = ImageUrl("https://images.example.com/photography/starry-night-sky.jpg")

        fun vintageCarCountryside() = ImageUrl("https://images.example.com/photography/vintage-car-countryside.jpg")

        fun lighthouseStormyCoast() = ImageUrl("https://images.example.com/photography/lighthouse-stormy-coast.jpg")

        fun cherryBlossomsSpring() = ImageUrl("https://images.example.com/photography/cherry-blossoms-spring.jpg")
    }

    object Art {
        fun abstractGeometricShapes() = ImageUrl("https://images.example.com/art/abstract-geometric-shapes.jpg")

        fun vintagePortraitPainting() = ImageUrl("https://images.example.com/art/vintage-portrait-painting.jpg")

        fun minimalistStillLife() = ImageUrl("https://images.example.com/art/minimalist-still-life.jpg")

        fun impressionistLandscape() = ImageUrl("https://images.example.com/art/impressionist-landscape.jpg")

        fun surrealDreamscape() = ImageUrl("https://images.example.com/art/surreal-dreamscape.jpg")

        fun modernSculpture() = ImageUrl("https://images.example.com/art/modern-sculpture.jpg")

        fun watercolorFlowers() = ImageUrl("https://images.example.com/art/watercolor-flowers.jpg")

        fun oilPaintingCityscape() = ImageUrl("https://images.example.com/art/oil-painting-cityscape.jpg")
    }

    object Activities {
        fun chefPreparingPasta() = ImageUrl("https://images.example.com/activities/chef-preparing-pasta.jpg")

        fun runnerCrossingBridge() = ImageUrl("https://images.example.com/activities/runner-crossing-bridge.jpg")

        fun artistPaintingCanvas() = ImageUrl("https://images.example.com/activities/artist-painting-canvas.jpg")

        fun dancerPerformingStage() = ImageUrl("https://images.example.com/activities/dancer-performing-stage.jpg")

        fun musicianPlayingPiano() = ImageUrl("https://images.example.com/activities/musician-playing-piano.jpg")

        fun gardenerTendingFlowers() = ImageUrl("https://images.example.com/activities/gardener-tending-flowers.jpg")

        fun writerCafe() = ImageUrl("https://images.example.com/activities/writer-cafe.jpg")

        fun photographerCaptureWildlife() = ImageUrl("https://images.example.com/activities/photographer-capture-wildlife.jpg")
    }

    object Simple {
        fun defaultImage() = ImageUrl("https://images.example.com/default.jpg")

        fun testImage() = ImageUrl("https://images.example.com/test.jpg")

        fun sampleImage() = ImageUrl("https://images.example.com/sample.jpg")

        fun placeholderImage() = ImageUrl("https://images.example.com/placeholder.jpg")
    }

    object EdgeCases {
        fun httpsUrl() = ImageUrl("https://secure.example.com/image.jpg")

        fun longFilename() = ImageUrl("https://images.example.com/very-long-filename-with-many-descriptive-words.jpg")

        fun withQueryParams() = ImageUrl("https://images.example.com/image.jpg?quality=high&format=webp")

        fun differentFormats() =
            listOf(
                ImageUrl("https://images.example.com/image.jpg"),
                ImageUrl("https://images.example.com/image.png"),
                ImageUrl("https://images.example.com/image.webp"),
                ImageUrl("https://images.example.com/image.gif"),
            )
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

    fun randomImage() =
        listOf(
            randomPhotography(),
            randomArt(),
            randomActivity(),
        ).random()
}
