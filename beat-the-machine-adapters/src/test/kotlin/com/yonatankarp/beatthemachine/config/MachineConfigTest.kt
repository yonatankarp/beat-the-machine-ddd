package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

private val runner =
    ApplicationContextRunner()
        .withInitializer(ConfigDataApplicationContextInitializer())
        .withUserConfiguration(MachineConfig::class.java)

val MachineConfigSuite by testSuite {
    given("the machine config") {
        whenever("using default local stable diffusion settings") {
            then("the checked-in application config matches the seed image generation policy") {
                runner.run { ctx ->
                    ctx.getBean(LocalStableDiffusionProperties::class.java) shouldBe
                        LocalStableDiffusionProperties(
                            baseUrl = "http://localhost:7860",
                            steps = 10,
                            width = 384,
                            height = 384,
                            timeoutSeconds = 300,
                            cfgScale = 7.0,
                            promptPrefix = "clear centered subject: ",
                            promptSuffix =
                                ", simple colorful storybook illustration, single main object or character, " +
                                    "recognizable silhouette, clean background, full object visible, sharp focus",
                            negativePrompt =
                                "abstract, blurry, distorted, duplicated, collage, pattern, grid, text, " +
                                    "watermark, cropped, low contrast, noisy",
                        )
                }
            }
        }

        whenever("no image provider is configured") {
            then("it defaults to the seed machine") {
                runner.run { ctx -> ctx.getBean(Machine::class.java).shouldBeInstanceOf<SeedMachine>() }
            }
        }

        whenever("the local-sd provider is configured") {
            then("it selects the local stable-diffusion machine") {
                runner
                    .withUserConfiguration(StubStorePictureConfig::class.java)
                    .withPropertyValues("btm.image.provider=local-sd", "btm.image.local-sd.base-url=http://localhost:7860")
                    .run { ctx -> ctx.getBean(Machine::class.java)::class.simpleName shouldBe "LocalStableDiffusionMachine" }
            }
        }
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
