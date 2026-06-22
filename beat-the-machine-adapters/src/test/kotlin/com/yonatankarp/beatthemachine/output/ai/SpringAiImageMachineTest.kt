package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImageGeneration
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImageResponse
import java.util.Base64

val SpringAiImageMachineSuite by testSuite {
    given("a Spring AI image machine") {
        whenever("the model returns a base64 image") {
            then("it decodes the image, stores it, and returns Ready") {
                val storePicture = mockk<StorePicture>()
                val imageModel = mockk<ImageModel>()
                val b64 = Base64.getEncoder().encodeToString(byteArrayOf(5, 6, 7))
                every { imageModel.call(any()) } returns ImageResponse(listOf(ImageGeneration(Image(null, b64))))
                coEvery { storePicture handle any<StorePicture.Command>() } returns "paid1"
                val machine = SpringAiImageMachine(imageModel, storePicture)
                val result = machine answer Machine.Query(Prompt("astronaut eating the moon"))
                result shouldBe Picture.Ready("/images/paid1")
            }
        }

        whenever("the model returns a url") {
            then("it fetches the image bytes and returns Ready") {
                val storePicture = mockk<StorePicture>()
                val imageModel = mockk<ImageModel>()
                val server = MockWebServer()
                server.start()
                val pngBytes = "PNGDATA".toByteArray()
                server.enqueue(MockResponse().setBody("PNGDATA"))
                every { imageModel.call(any()) } returns
                    ImageResponse(listOf(ImageGeneration(Image(server.url("/img.png").toString(), null))))
                val saved = slot<StorePicture.Command>()
                coEvery { storePicture handle capture(saved) } returns "paid2"
                val machine = SpringAiImageMachine(imageModel, storePicture, imageWebClient())
                val result = machine answer Machine.Query(Prompt("astronaut eating the moon"))
                result shouldBe Picture.Ready("/images/paid2")
                pngBytes.contentEquals(saved.captured.bytes).shouldBeTrue()
                server.shutdown()
            }
        }

        whenever("the model fails") {
            then("it yields Failed") {
                val storePicture = mockk<StorePicture>()
                val imageModel = mockk<ImageModel>()
                every { imageModel.call(any()) } throws RuntimeException("boom")
                val machine = SpringAiImageMachine(imageModel, storePicture)
                machine answer Machine.Query(Prompt("anything")) shouldBe Picture.Failed
            }
        }
    }
}
