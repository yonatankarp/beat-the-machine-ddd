package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import java.net.InetAddress
import java.net.URI
import java.util.Base64

class SpringAiImageMachine(
    private val imageModel: ImageModel,
    private val storePicture: StorePicture,
    private val webClient: WebClient = imageWebClient(),
    private val imageUrlPolicy: ImageUrlPolicy = ImageUrlPolicy.Default,
) : Machine {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun answer(query: Machine.Query): Picture =
        storePicture.renderedPicture(logger, query.prompt) {
            withContext(Dispatchers.IO) {
                imageModel.call(ImagePrompt(query.prompt.text)).result?.output
            }?.resolvedBytes()
        }

    private suspend fun Image.resolvedBytes(): ByteArray? {
        val encodedImage = b64Json
        if (!encodedImage.isNullOrBlank()) {
            return Base64.getDecoder().decode(encodedImage)
        }

        val imageUrl = url
        if (imageUrl.isNullOrBlank()) return null

        val uri = URI.create(imageUrl)
        if (!imageUrlPolicy.allows(uri)) return null

        val response =
            webClient
                .get()
                .uri(uri)
                .retrieve()
                .awaitEntity<ByteArray>()
        if (response.headers.contentType?.isImage() != true) return null
        return response.body
    }
}

fun interface ImageUrlPolicy {
    fun allows(uri: URI): Boolean

    object Default : ImageUrlPolicy {
        override fun allows(uri: URI): Boolean {
            if (uri.scheme != "https") return false
            val host = uri.host ?: return false
            return runCatching {
                InetAddress.getAllByName(host).none { it.isPrivateNetworkAddress() }
            }.getOrDefault(false)
        }
    }

    object AllowAll : ImageUrlPolicy {
        override fun allows(uri: URI): Boolean = true
    }
}

private fun InetAddress.isPrivateNetworkAddress(): Boolean =
    isAnyLocalAddress ||
        isLoopbackAddress ||
        isLinkLocalAddress ||
        isSiteLocalAddress ||
        isMulticastAddress

private fun MediaType.isImage(): Boolean = type.equals("image", ignoreCase = true)
