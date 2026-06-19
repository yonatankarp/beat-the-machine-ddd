package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.Base64

/**
 * Renders a [Picture] via a hosted Spring AI [ImageModel] (e.g. OpenAI DALL-E or
 * Stability). Providers return either inline base64 or a URL; both are resolved to
 * bytes and persisted through [PictureStore] so serving is uniform with the local
 * path. Failures map to [Picture.Failed].
 */
class SpringAiImageMachine(
    private val imageModel: ImageModel,
    private val pictureStore: PictureStore,
) : Machine {
    private val logger = LoggerFactory.getLogger(javaClass)

    // The adapter owns its transport, used only to fetch a provider-returned image
    // URL. The decode buffer is raised since image bytes can exceed WebClient's
    // 256 KB default.
    private val webClient =
        WebClient
            .builder()
            .codecs { it.defaultCodecs().maxInMemorySize(MAX_RESPONSE_BYTES) }
            .build()

    override suspend fun generate(prompt: Prompt): Picture =
        try {
            val image =
                withContext(Dispatchers.IO) {
                    imageModel.call(ImagePrompt(prompt.text)).result?.output
                } ?: return Picture.Failed
            val bytes =
                when {
                    !image.b64Json.isNullOrBlank() -> {
                        Base64.getDecoder().decode(image.b64Json)
                    }

                    !image.url.isNullOrBlank() -> {
                        val url = image.url ?: return Picture.Failed
                        webClient
                            .get()
                            .uri(url)
                            .retrieve()
                            .awaitBody<ByteArray>()
                    }

                    else -> {
                        return Picture.Failed
                    }
                }
            Picture.Ready(pictureStore.save(bytes, "image/png"))
        } catch (e: Exception) {
            logger.warn("Paid image generation failed for prompt '{}'", prompt.text, e)
            Picture.Failed
        }

    private companion object {
        // 16 MB: comfortably fits a provider-returned image.
        const val MAX_RESPONSE_BYTES = 16 * 1024 * 1024
    }
}
