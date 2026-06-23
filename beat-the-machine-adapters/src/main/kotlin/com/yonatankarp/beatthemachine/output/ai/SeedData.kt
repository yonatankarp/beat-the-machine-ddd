package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

internal data class SeedChallenge(
    val id: String,
    val difficulty: Difficulty,
    val prompt: Prompt,
    val pictureUrl: String,
)

internal val SEED: List<SeedChallenge> =
    listOf(
        seed(Difficulty.EASY, 1, "red robot"),
        seed(Difficulty.EASY, 2, "blue castle"),
        seed(Difficulty.EASY, 3, "sleepy dragon"),
        seed(Difficulty.EASY, 4, "golden guitar"),
        seed(Difficulty.EASY, 5, "paper boat"),
        seed(Difficulty.EASY, 6, "tiny volcano"),
        seed(Difficulty.EASY, 7, "green umbrella"),
        seed(Difficulty.EASY, 8, "moon rabbit"),
        seed(Difficulty.EASY, 9, "glass apple"),
        seed(Difficulty.EASY, 10, "orange submarine"),
        seed(Difficulty.MEDIUM, 1, "astronaut eating noodles"),
        seed(Difficulty.MEDIUM, 2, "dolphin wearing sunglasses"),
        seed(Difficulty.MEDIUM, 3, "wizard painting stars"),
        seed(Difficulty.MEDIUM, 4, "penguin riding scooter"),
        seed(Difficulty.MEDIUM, 5, "pirate baking cupcakes"),
        seed(Difficulty.MEDIUM, 6, "robot watering flowers"),
        seed(Difficulty.MEDIUM, 7, "dragon reading newspaper"),
        seed(Difficulty.MEDIUM, 8, "octopus playing chess"),
        seed(Difficulty.MEDIUM, 9, "ghost holding lantern"),
        seed(Difficulty.MEDIUM, 10, "cowboy fixing spaceship"),
        seed(Difficulty.HARD, 1, "clock tower made of pancakes"),
        seed(Difficulty.HARD, 2, "detective fox in rainy paris"),
        seed(Difficulty.HARD, 3, "mermaid repairing a bicycle"),
        seed(Difficulty.HARD, 4, "haunted library under the ocean"),
        seed(Difficulty.HARD, 5, "knight juggling crystal planets"),
        seed(Difficulty.HARD, 6, "garden party on mars"),
        seed(Difficulty.HARD, 7, "giant teapot in the desert"),
        seed(Difficulty.HARD, 8, "violin concert for mushrooms"),
        seed(Difficulty.HARD, 9, "train station inside a tree"),
        seed(Difficulty.HARD, 10, "ancient computer predicting weather"),
    )

private fun seed(
    difficulty: Difficulty,
    number: Int,
    prompt: String,
): SeedChallenge =
    seedChallenge(
        id = "seed-${difficulty.name.lowercase()}-${number.toString().padStart(2, '0')}",
        difficulty = difficulty,
        prompt = Prompt(prompt),
    )

private fun seedChallenge(
    id: String,
    difficulty: Difficulty,
    prompt: Prompt,
): SeedChallenge =
    SeedChallenge(
        id = id,
        difficulty = difficulty,
        prompt = prompt,
        pictureUrl = "/seed/images/$id.png",
    )
