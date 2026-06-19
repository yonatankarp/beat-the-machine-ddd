package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.output.ai.SeedPromptSource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.assertTrue

class PromptSourceConfigTest {
    private val runner = ApplicationContextRunner().withUserConfiguration(PromptSourceConfig::class.java)

    @Test
    fun `defaults to the seed prompt source`() {
        runner.run { ctx -> assertTrue(ctx.getBean(PromptSource::class.java) is SeedPromptSource) }
    }
}
