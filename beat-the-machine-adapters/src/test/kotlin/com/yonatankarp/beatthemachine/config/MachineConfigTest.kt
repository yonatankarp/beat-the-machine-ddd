package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

private val runner = ApplicationContextRunner().withUserConfiguration(MachineConfig::class.java)

val MachineConfigSuite by testSuite {
    test("defaults to the seed machine") {
        runner.run { ctx -> ctx.getBean(Machine::class.java).shouldBeInstanceOf<SeedMachine>() }
    }

    test("selects local-sd when configured") {
        runner
            .withUserConfiguration(StubStorePictureConfig::class.java)
            .withPropertyValues("btm.image.provider=local-sd", "btm.image.local-sd.base-url=http://localhost:7860")
            .run { ctx -> ctx.getBean(Machine::class.java)::class.simpleName shouldBe "LocalStableDiffusionMachine" }
    }
}

@TestConfiguration
class StubStorePictureConfig {
    @Bean
    fun storePicture(): StorePicture =
        object : StorePicture {
            override suspend fun handle(command: StorePicture.Command): String = "/stub-url"
        }
}
