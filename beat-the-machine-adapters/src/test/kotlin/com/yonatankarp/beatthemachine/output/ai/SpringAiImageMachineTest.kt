package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.testballoon.gwt.action
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.setup
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
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
                server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody("PNGDATA"))
                every { imageModel.call(any()) } returns
                    ImageResponse(listOf(ImageGeneration(Image(server.url("/img.png").toString(), null))))
                val saved = slot<StorePicture.Command>()
                coEvery { storePicture handle capture(saved) } returns "paid2"
                val machine = SpringAiImageMachine(imageModel, storePicture, imageWebClient(), ImageUrlPolicy.AllowAll)
                val result = machine answer Machine.Query(Prompt("astronaut eating the moon"))
                result shouldBe Picture.Ready("/images/paid2")
                pngBytes.contentEquals(saved.captured.bytes).shouldBeTrue()
                server.shutdown()
            }
        }

        given("the model returns an unsafe url") {
            val server by setup {
                MockWebServer().apply { start() }
            }
            val storePicture by setup { mockk<StorePicture>() }
            val imageModel by setup {
                mockk<ImageModel>().also {
                    every { it.call(any()) } returns
                        ImageResponse(listOf(ImageGeneration(Image(server.url("/private.png").toString(), null))))
                }
            }
            val machine by setup {
                SpringAiImageMachine(imageModel, storePicture, imageWebClient())
            }

            whenever("answering the prompt") {
                val result by action {
                    machine answer Machine.Query(Prompt("astronaut eating the moon"))
                }

                then("it rejects the url without fetching it") {
                    result shouldBe Picture.Failed
                    server.requestCount shouldBe 0
                    server.shutdown()
                }
            }
        }

        given("the model url returns a non-image content type") {
            val server by setup {
                MockWebServer().apply {
                    start()
                    enqueue(MockResponse().setHeader("Content-Type", "text/plain").setBody("not image"))
                }
            }
            val storePicture by setup { mockk<StorePicture>() }
            val imageModel by setup {
                mockk<ImageModel>().also {
                    every { it.call(any()) } returns
                        ImageResponse(listOf(ImageGeneration(Image(server.url("/img.txt").toString(), null))))
                }
            }
            val machine by setup {
                SpringAiImageMachine(imageModel, storePicture, imageWebClient(), ImageUrlPolicy.AllowAll)
            }

            whenever("answering the prompt") {
                val result by action {
                    machine answer Machine.Query(Prompt("astronaut eating the moon"))
                }

                then("it rejects the response") {
                    result shouldBe Picture.Failed
                    server.shutdown()
                }
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

        whenever("the image work is cancelled") {
            then("it propagates cancellation") {
                val storePicture = mockk<StorePicture>()
                val imageModel = mockk<ImageModel>()
                every { imageModel.call(any()) } throws CancellationException("stopping")
                val machine = SpringAiImageMachine(imageModel, storePicture)
                shouldThrow<CancellationException> {
                    machine answer Machine.Query(Prompt("anything"))
                }
            }
        }
    }
}
