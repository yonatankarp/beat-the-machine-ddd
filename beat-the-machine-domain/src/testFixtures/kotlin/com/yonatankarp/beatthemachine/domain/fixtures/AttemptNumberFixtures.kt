package com.yonatankarp.beatthemachine.domain.fixtures

import com.yonatankarp.beatthemachine.domain.game.AttemptNumber

object AttemptNumberFixtures {
    object Standard {
        fun zero() = AttemptNumber(0)

        fun one() = AttemptNumber(1)

        fun two() = AttemptNumber(2)

        fun three() = AttemptNumber(3)

        fun five() = AttemptNumber(5)

        fun ten() = AttemptNumber(10)
    }

    object EdgeCases {
        fun minimum() = AttemptNumber(0)

        fun large() = AttemptNumber(100)

        fun veryLarge() = AttemptNumber(999)
    }

    object Scenarios {
        fun firstAttempt() = AttemptNumber(0)

        fun secondAttempt() = AttemptNumber(1)

        fun midGameAttempt() = AttemptNumber(3)

        fun nearMaxAttempt() = AttemptNumber(4)

        fun finalAttempt() = AttemptNumber(5)
    }

    fun randomStandard() =
        listOf(
            Standard.one(),
            Standard.two(),
            Standard.three(),
            Standard.five(),
        ).random()

    fun randomScenario() =
        listOf(
            Scenarios.firstAttempt(),
            Scenarios.secondAttempt(),
            Scenarios.midGameAttempt(),
            Scenarios.nearMaxAttempt(),
        ).random()
}
