package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.test.dsl.asPrompt
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val SeedMachineSuite by testSuite {
    val machine = SeedMachine()

    given("a seed machine") {
        whenever("answering a known seeded prompt") {
            then("it returns the curated url") {
                val seed = SEED.first()
                val prompt = seed.prompt
                val url = seed.pictureUrl
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
