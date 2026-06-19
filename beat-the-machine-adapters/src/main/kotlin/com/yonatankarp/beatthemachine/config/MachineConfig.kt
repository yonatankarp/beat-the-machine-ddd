package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.output.ai.LocalStableDiffusionMachine
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import com.yonatankarp.beatthemachine.output.ai.SpringAiImageMachine
import org.springframework.ai.image.ImageModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import kotlin.time.Duration.Companion.seconds

@Configuration
class MachineConfig {
    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "seed", matchIfMissing = true)
    fun seedMachine(): Machine = SeedMachine()

    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "local-sd")
    fun localStableDiffusionMachine(
        pictureStore: PictureStore,
        @Value("\${btm.image.local-sd.base-url}") baseUrl: String,
        @Value("\${btm.image.local-sd.steps:8}") steps: Int,
        @Value("\${btm.image.local-sd.width:512}") width: Int,
        @Value("\${btm.image.local-sd.height:512}") height: Int,
        @Value("\${btm.image.local-sd.timeout-seconds:120}") timeoutSeconds: Long,
    ): Machine =
        LocalStableDiffusionMachine(
            WebClient.builder().baseUrl(baseUrl).build(),
            pictureStore,
            steps,
            width,
            height,
            timeoutSeconds.seconds,
        )

    @Bean
    @ConditionalOnProperty(name = ["btm.image.provider"], havingValue = "paid")
    fun springAiImageMachine(
        imageModel: ImageModel,
        pictureStore: PictureStore,
    ): Machine = SpringAiImageMachine(imageModel, pictureStore, WebClient.builder().build())
}
