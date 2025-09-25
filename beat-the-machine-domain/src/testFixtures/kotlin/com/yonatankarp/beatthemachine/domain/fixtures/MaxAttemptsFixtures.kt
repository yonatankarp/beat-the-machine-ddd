package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.game.MaxAttempts

object MaxAttemptsFixtures {
    object Standard {
        fun three() = MaxAttempts(3)

        fun five() = MaxAttempts(5)

        fun ten() = MaxAttempts(10)

        fun twenty() = MaxAttempts(20)
    }

    object GameModes {
        fun easy() = MaxAttempts(10)

        fun normal() = MaxAttempts(5)

        fun hard() = MaxAttempts(3)

        fun expert() = MaxAttempts(1)
    }

    object EdgeCases {
        fun minimum() = MaxAttempts(1)

        fun large() = MaxAttempts(100)

        fun veryLarge() = MaxAttempts(999)
    }

    object Testing {
        fun default() = MaxAttempts(5)

        fun unlimited() = MaxAttempts(999)

        fun quickTest() = MaxAttempts(2)
    }

    fun randomStandard() =
        listOf(
            Standard.three(),
            Standard.five(),
            Standard.ten(),
        ).random()

    fun randomGameMode() =
        listOf(
            GameModes.easy(),
            GameModes.normal(),
            GameModes.hard(),
        ).random()
}
