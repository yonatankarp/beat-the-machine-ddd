package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
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
        // Given
        val configured =
            runner
                .withUserConfiguration(StubPictureStoreConfig::class.java)
                .withPropertyValues("btm.image.provider=local-sd", "btm.image.local-sd.base-url=http://localhost:7860")

        // When / Then
        configured.run { ctx -> assertTrue(ctx.getBean(Machine::class.java)::class.simpleName == "LocalStableDiffusionMachine") }
    }

    @Configuration
    class StubPictureStoreConfig {
        @Bean
        fun pictureStore(): PictureStore =
            object : PictureStore {
                override suspend fun save(
                    bytes: ByteArray,
                    contentType: String,
                ): String = "stub-url"

                override suspend fun load(id: String): StoredImage? = null
            }
    }
}
