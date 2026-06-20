package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.test.assertTrue

class MachineConfigTest {
    private val runner = ApplicationContextRunner().withUserConfiguration(MachineConfig::class.java)

    @Test
    fun `defaults to the seed machine`() {
        runner.run { ctx -> assertTrue(ctx.getBean(Machine::class.java) is SeedMachine) }
    }

    @Test
    fun `selects local-sd when configured`() {
        runner
            .withUserConfiguration(StubStorePictureConfig::class.java)
            .withPropertyValues("btm.image.provider=local-sd", "btm.image.local-sd.base-url=http://localhost:7860")
            .run { ctx -> assertTrue(ctx.getBean(Machine::class.java)::class.simpleName == "LocalStableDiffusionMachine") }
    }

    @Configuration
    class StubStorePictureConfig {
        @Bean
        fun storePicture(): StorePicture =
            object : StorePicture {
                override suspend fun handle(command: StorePicture.Command): String = "/stub-url"
            }
    }
}
