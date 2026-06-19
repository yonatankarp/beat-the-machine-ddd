package com.yonatankarp.beatthemachine.domain.valueobject

enum class Difficulty(
    val livesMultiplier: Double,
) {
    EASY(1.5),
    MEDIUM(1.0),
    HARD(0.7),
}
