package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import org.slf4j.Logger
import org.springframework.web.reactive.function.client.WebClient

internal const val MAX_IMAGE_BYTES = 16 * 1024 * 1024

internal const val PNG_CONTENT_TYPE = "image/png"

internal const val IMAGE_PATH_PREFIX = "/images/"

internal fun imageUrl(id: String): String = "$IMAGE_PATH_PREFIX$id"

internal fun imageWebClient(baseUrl: String? = null): WebClient {
    val builder = WebClient.builder()
    if (baseUrl != null) builder.baseUrl(baseUrl)
    return builder
        .codecs { it.defaultCodecs().maxInMemorySize(MAX_IMAGE_BYTES) }
        .build()
}

internal suspend fun StorePicture.renderedPicture(
    logger: Logger,
    prompt: Prompt,
    produce: suspend () -> ByteArray?,
): Picture =
    try {
        val bytes = produce() ?: return Picture.Failed
        Picture.Ready(imageUrl(this handle StorePicture.Command(bytes, PNG_CONTENT_TYPE)))
    } catch (e: Exception) {
        logger.warn("Image generation failed for prompt '{}'", prompt.text, e)
        Picture.Failed
    }
