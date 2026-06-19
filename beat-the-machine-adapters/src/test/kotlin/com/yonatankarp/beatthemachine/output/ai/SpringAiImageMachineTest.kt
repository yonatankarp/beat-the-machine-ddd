package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageGeneration
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImageResponse
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64
import kotlin.test.assertEquals

class SpringAiImageMachineTest {
    private val pictureStore = mockk<PictureStore>()
    private val imageModel = mockk<ImageModel>()
    private val webClient = WebClient.builder().build()

    @Test
    fun `decodes b64 image, stores it, and returns Ready`() =
        runTest {
            val b64 = Base64.getEncoder().encodeToString(byteArrayOf(5, 6, 7))
            every { imageModel.call(any()) } returns ImageResponse(listOf(ImageGeneration(Image(null, b64))))
            coEvery { pictureStore.save(any(), "image/png") } returns "/images/paid1"

            val machine = SpringAiImageMachine(imageModel, pictureStore, webClient)
            assertEquals(Picture.Ready("/images/paid1"), machine.generate(Prompt("astronaut eating the moon")))
        }

    @Test
    fun `model failure yields Failed`() =
        runTest {
            every { imageModel.call(any()) } throws RuntimeException("boom")
            val machine = SpringAiImageMachine(imageModel, pictureStore, webClient)
            assertEquals(Picture.Failed, machine.generate(Prompt("anything")))
        }
}
