package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val SeedMachineSuite by testSuite {
    val machine = SeedMachine()

    test("returns the curated url for a known prompt") {
        val (prompt, url) = SEED.first()
        val result = machine answer Machine.Query(prompt)
        result shouldBe Picture.Ready(url)
    }

    test("returns Failed for an unknown prompt") {
        val prompt = "a prompt that is not seeded".asPrompt()
        val result = machine answer Machine.Query(prompt)
        result shouldBe Picture.Failed
    }
}
