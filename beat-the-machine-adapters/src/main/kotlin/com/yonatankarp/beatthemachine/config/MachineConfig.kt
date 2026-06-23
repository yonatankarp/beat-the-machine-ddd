package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.output.ai.LocalStableDiffusionMachine
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import com.yonatankarp.beatthemachine.output.ai.SpringAiImageMachine
import org.springframework.ai.image.ImageModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Duration.Companion.seconds

@Configuration
@EnableConfigurationProperties(LocalStableDiffusionProperties::class)
class MachineConfig {
    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "seed", matchIfMissing = true)
    fun seedMachine(): Machine = SeedMachine()

    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "local-sd")
    fun localStableDiffusionMachine(
        storePicture: StorePicture,
        properties: LocalStableDiffusionProperties,
    ): Machine =
        LocalStableDiffusionMachine(
            properties.baseUrl,
            storePicture,
            properties.steps,
            properties.width,
            properties.height,
            properties.timeoutSeconds.seconds,
            properties.promptPrefix,
            properties.promptSuffix,
            properties.negativePrompt,
            properties.cfgScale,
        )

    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "paid")
    fun springAiImageMachine(
        imageModel: ImageModel,
        storePicture: StorePicture,
    ): Machine = SpringAiImageMachine(imageModel, storePicture)
}
