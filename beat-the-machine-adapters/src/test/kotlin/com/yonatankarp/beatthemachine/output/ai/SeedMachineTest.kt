package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val SeedMachineSuite by testSuite {
    val machine = SeedMachine()

    given("a seed machine") {
        whenever("answering a known seeded prompt") {
            then("it returns the curated url") {
                val (prompt, url) = SEED.first()
                val result = machine answer Machine.Query(prompt)
                result shouldBe Picture.Ready(url)
            }
        }

        whenever("answering an unknown prompt") {
            then("it returns Failed") {
                val prompt = "a prompt that is not seeded".asPrompt()
                val result = machine answer Machine.Query(prompt)
                result shouldBe Picture.Failed
            }
        }
    }
}
