package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val SeedMachineSuite by testSuite {
    val machine = SeedMachine()

    test("returns the curated url for a known prompt") {
        // Given
        val (prompt, url) = SEED.first()

        // When
        val result = machine.generate(prompt)

        // Then
        result shouldBe Picture.Ready(url)
    }

    test("returns Failed for an unknown prompt") {
        // Given
        val prompt = "a prompt that is not seeded".asPrompt()

        // When
        val result = machine.generate(prompt)

        // Then
        result shouldBe Picture.Failed
    }
}
