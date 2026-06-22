package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.output.ai.SeedPromptSource
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.runner.ApplicationContextRunner

private val runner = ApplicationContextRunner().withUserConfiguration(PromptSourceConfig::class.java)

val PromptSourceConfigSuite by testSuite {
    given("the prompt source config") {
        whenever("no prompt source is configured") {
            then("it defaults to the seed prompt source") {
                runner.run { ctx -> ctx.getBean(PromptSource::class.java).shouldBeInstanceOf<SeedPromptSource>() }
            }
        }
    }
}
