package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.nio.file.Files
import java.nio.file.Path

val SeedPromptSourceSuite by testSuite {
    given("the seed catalog") {
        whenever("counting curated entries") {
            then("it contains thirty prompts split evenly across difficulties") {
                SEED.size shouldBe 30
                Difficulty.entries.forEach { difficulty ->
                    SEED.count { it.difficulty == difficulty } shouldBe 10
                }
            }
        }

        whenever("checking image locations") {
            then("every seed uses a bundled local image") {
                SEED.forEach { seed ->
                    seed.pictureUrl shouldStartWith "/seed/images/"
                    listOf(
                        Path.of("src/main/resources", "static${seed.pictureUrl}"),
                        Path.of("beat-the-machine-adapters/src/main/resources", "static${seed.pictureUrl}"),
                    ).any(Files::isRegularFile).shouldBeTrue()
                }
            }
        }
    }

    given("a seed prompt source") {
        whenever("answering a prompt query") {
            then("it returns a prompt from the requested difficulty") {
                repeat(20) {
                    val prompt = SeedPromptSource() answer PromptSource.Query(Difficulty.HARD)
                    val seedPrompts = SEED.filter { it.difficulty == Difficulty.HARD }.map { it.prompt }.toSet()
                    (prompt in seedPrompts).shouldBeTrue()
                }
            }
        }
    }
}
