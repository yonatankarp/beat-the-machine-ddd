package com.yonatankarp.beatthemachine.output.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.Base64
import kotlin.time.Duration

class LocalStableDiffusionMachine(
    baseUrl: String,
    private val storePicture: StorePicture,
    private val steps: Int,
    private val width: Int,
    private val height: Int,
    private val timeout: Duration,
) : Machine {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = imageWebClient(baseUrl)

    override suspend fun answer(query: Machine.Query): Picture =
        storePicture.renderedPicture(logger, query.prompt) {
            val response =
                withContext(Dispatchers.IO) {
                    withTimeout(timeout) {
                        webClient.textToImage(Txt2ImgRequest(query.prompt.text, steps, width, height))
                    }
                }
            val b64 = response.images.firstOrNull() ?: return@renderedPicture null
            Base64.getDecoder().decode(b64)
        }

    private suspend fun WebClient.textToImage(request: Txt2ImgRequest): Txt2ImgResponse =
        post()
            .uri("/sdapi/v1/txt2img")
            .bodyValue(request)
            .retrieve()
            .awaitBody()

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
