package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class LocalStableDiffusionMachineTest {
    private lateinit var server: MockWebServer
    private val pictureStore = mockk<PictureStore>()

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun machine(): LocalStableDiffusionMachine {
        val webClient = WebClient.builder().baseUrl(server.url("/").toString()).build()
        return LocalStableDiffusionMachine(webClient, pictureStore, steps = 8, width = 512, height = 512, timeout = 5.seconds)
    }

    @Test
    fun `stores the decoded image and returns Ready`() =
        runTest {
            val pngBytes = byteArrayOf(1, 2, 3)
            val b64 = Base64.getEncoder().encodeToString(pngBytes)
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"images":["$b64"]}"""),
            )
            coEvery { pictureStore.save(any(), "image/png") } returns "/images/xyz"

            val result = machine().generate(Prompt("dragon eating a cookie"))

            assertEquals(Picture.Ready("/images/xyz"), result)
        }

    @Test
    fun `server error yields Failed`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))
            assertEquals(Picture.Failed, machine().generate(Prompt("anything")))
        }

    @Test
    fun `empty image list yields Failed`() =
        runTest {
            server.enqueue(
                MockResponse().setHeader("Content-Type", "application/json").setBody("""{"images":[]}"""),
            )
            assertEquals(Picture.Failed, machine().generate(Prompt("anything")))
        }
}
