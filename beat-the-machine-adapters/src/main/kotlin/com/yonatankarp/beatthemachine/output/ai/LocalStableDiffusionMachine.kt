package com.yonatankarp.beatthemachine.output.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.Base64
import kotlin.time.Duration

/**
 * Renders a [Picture] by calling a local Automatic1111 Stable Diffusion server's
 * /sdapi/v1/txt2img endpoint, which returns base64-encoded PNGs. The decoded bytes
 * are handed to [PictureStore], which yields the servable URL. Any failure
 * (network, timeout, non-2xx, empty result, bad base64) maps to [Picture.Failed]
 * so the picture pipeline records it cleanly rather than hanging.
 */
class LocalStableDiffusionMachine(
    private val webClient: WebClient,
    private val pictureStore: PictureStore,
    private val steps: Int,
    private val width: Int,
    private val height: Int,
    private val timeout: Duration,
) : Machine {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(prompt: Prompt): Picture =
        try {
            val response =
                withContext(Dispatchers.IO) {
                    withTimeout(timeout) {
                        webClient
                            .post()
                            .uri("/sdapi/v1/txt2img")
                            .bodyValue(Txt2ImgRequest(prompt.text, steps, width, height))
                            .retrieve()
                            .awaitBody<Txt2ImgResponse>()
                    }
                }
            val b64 = response.images.firstOrNull() ?: return Picture.Failed
            val bytes = Base64.getDecoder().decode(b64)
            Picture.Ready(pictureStore.save(bytes, "image/png"))
        } catch (e: Exception) {
            logger.warn("Local SD generation failed for prompt '{}'", prompt.text, e)
            Picture.Failed
        }

    private data class Txt2ImgRequest(
        val prompt: String,
        val steps: Int,
        val width: Int,
        val height: Int,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Txt2ImgResponse(
        val images: List<String> = emptyList(),
    )
}
