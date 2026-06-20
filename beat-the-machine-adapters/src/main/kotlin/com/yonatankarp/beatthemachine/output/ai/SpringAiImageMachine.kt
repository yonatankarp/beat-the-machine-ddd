package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.Base64

class SpringAiImageMachine(
    private val imageModel: ImageModel,
    private val pictureStore: PictureStore,
    private val webClient: WebClient = imageWebClient(),
) : Machine {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun generate(prompt: Prompt): Picture =
        pictureStore.renderedPicture(logger, prompt) {
            withContext(Dispatchers.IO) {
                imageModel.call(ImagePrompt(prompt.text)).result?.output
            }?.resolvedBytes()
        }

    private suspend fun Image.resolvedBytes(): ByteArray? =
        when {
            !b64Json.isNullOrBlank() -> {
                Base64.getDecoder().decode(b64Json)
            }

            !url.isNullOrBlank() -> {
                webClient
                    .get()
                    .uri(url!!)
                    .retrieve()
                    .awaitBody<ByteArray>()
            }

            else -> {
                null
            }
        }
}
