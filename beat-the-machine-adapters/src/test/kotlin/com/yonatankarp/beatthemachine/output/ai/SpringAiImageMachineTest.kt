package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageGeneration
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImageResponse
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpringAiImageMachineTest {
    private val storePicture = mockk<StorePicture>()
    private val imageModel = mockk<ImageModel>()

    @Test
    fun `decodes b64 image, stores it, and returns Ready`() =
        runTest {
            // Given
            val b64 = Base64.getEncoder().encodeToString(byteArrayOf(5, 6, 7))
            every { imageModel.call(any()) } returns ImageResponse(listOf(ImageGeneration(Image(null, b64))))
            coEvery { storePicture handle any<StorePicture.Command>() } returns "paid1"

            // When
            val machine = SpringAiImageMachine(imageModel, storePicture)
            val result = machine answer Machine.Query(Prompt("astronaut eating the moon"))

            // Then
            assertEquals(Picture.Ready("/images/paid1"), result)
        }

    @Test
    fun `fetches image bytes when the model returns a url`() =
        runTest {
            // Given
            val server = MockWebServer()
            server.start()
            val pngBytes = "PNGDATA".toByteArray()
            server.enqueue(MockResponse().setBody("PNGDATA"))
            every { imageModel.call(any()) } returns
                ImageResponse(listOf(ImageGeneration(Image(server.url("/img.png").toString(), null))))
            val saved = slot<StorePicture.Command>()
            coEvery { storePicture handle capture(saved) } returns "paid2"

            // When
            val machine = SpringAiImageMachine(imageModel, storePicture, imageWebClient())
            val result = machine answer Machine.Query(Prompt("astronaut eating the moon"))

            // Then
            assertEquals(Picture.Ready("/images/paid2"), result)
            assertTrue(pngBytes.contentEquals(saved.captured.bytes))
            server.shutdown()
        }

    @Test
    fun `model failure yields Failed`() =
        runTest {
            // Given
            every { imageModel.call(any()) } throws RuntimeException("boom")

            // When
            val machine = SpringAiImageMachine(imageModel, storePicture)

            // Then
            assertEquals(Picture.Failed, machine answer Machine.Query(Prompt("anything")))
        }
}
