package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.riddle.Word

object WordFixtures {
    object Nature {
        fun sunset() = Word("sunset")

        fun beach() = Word("beach")

        fun mountain() = Word("mountain")

        fun forest() = Word("forest")

        fun lake() = Word("lake")

        fun ocean() = Word("ocean")

        fun river() = Word("river")

        fun valley() = Word("valley")

        fun meadow() = Word("meadow")

        fun canyon() = Word("canyon")
    }

    object Colors {
        fun golden() = Word("golden")

        fun crimson() = Word("crimson")

        fun azure() = Word("azure")

        fun emerald() = Word("emerald")

        fun violet() = Word("violet")

        fun amber() = Word("amber")

        fun silver() = Word("silver")

        fun turquoise() = Word("turquoise")
    }

    object Art {
        fun abstract() = Word("abstract")

        fun portrait() = Word("portrait")

        fun landscape() = Word("landscape")

        fun geometric() = Word("geometric")

        fun vintage() = Word("vintage")

        fun minimalist() = Word("minimalist")

        fun impressionist() = Word("impressionist")

        fun surreal() = Word("surreal")
    }

    object Activities {
        fun painting() = Word("painting")

        fun dancing() = Word("dancing")

        fun cooking() = Word("cooking")

        fun running() = Word("running")

        fun reading() = Word("reading")

        fun writing() = Word("writing")

        fun singing() = Word("singing")

        fun walking() = Word("walking")
    }

    object Objects {
        fun bridge() = Word("bridge")

        fun castle() = Word("castle")

        fun lighthouse() = Word("lighthouse")

        fun waterfall() = Word("waterfall")

        fun garden() = Word("garden")

        fun pathway() = Word("pathway")

        fun cottage() = Word("cottage")

        fun temple() = Word("temple")
    }

    object Simple {
        fun hello() = Word("hello")

        fun world() = Word("world")

        fun test() = Word("test")

        fun example() = Word("example")

        fun sample() = Word("sample")
    }

    object EdgeCases {
        fun singleLetter() = Word("a")

        fun longWord() = Word("extraordinary")

        fun withNumbers() = Word("word123")

        fun specialChars() = Word("café")
    }

    fun randomNature() =
        listOf(
            Nature.sunset(),
            Nature.beach(),
            Nature.mountain(),
            Nature.forest(),
            Nature.lake(),
            Nature.ocean(),
            Nature.river(),
            Nature.valley(),
        ).random()

    fun randomColor() =
        listOf(
            Colors.golden(),
            Colors.crimson(),
            Colors.azure(),
            Colors.emerald(),
            Colors.violet(),
            Colors.amber(),
            Colors.silver(),
            Colors.turquoise(),
        ).random()

    fun randomArt() =
        listOf(
            Art.abstract(),
            Art.portrait(),
            Art.landscape(),
            Art.geometric(),
            Art.vintage(),
            Art.minimalist(),
            Art.impressionist(),
            Art.surreal(),
        ).random()
}
