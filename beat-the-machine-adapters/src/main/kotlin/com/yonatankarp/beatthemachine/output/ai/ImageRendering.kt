package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.netty.channel.ChannelOption
import kotlinx.coroutines.CancellationException
import org.slf4j.Logger
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

internal const val MAX_IMAGE_BYTES = 16 * 1024 * 1024

internal const val PNG_CONTENT_TYPE = "image/png"

internal const val IMAGE_PATH_PREFIX = "/images/"

private val IMAGE_REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)

private const val IMAGE_CONNECT_TIMEOUT_MILLIS = 5000

internal fun imageUrl(id: String): String = "$IMAGE_PATH_PREFIX$id"

internal fun imageWebClient(baseUrl: String? = null): WebClient {
    val httpClient =
        HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, IMAGE_CONNECT_TIMEOUT_MILLIS)
            .responseTimeout(IMAGE_REQUEST_TIMEOUT)
    val builder = WebClient.builder()
    if (baseUrl != null) builder.baseUrl(baseUrl)
    return builder
        .clientConnector(ReactorClientHttpConnector(httpClient))
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
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn("Image generation failed for prompt '{}'", prompt.text, e)
        Picture.Failed
    }
