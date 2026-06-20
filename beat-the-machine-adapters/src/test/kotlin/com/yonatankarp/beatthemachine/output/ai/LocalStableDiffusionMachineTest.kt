package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class LocalStableDiffusionMachineTest {
    private val storePicture = mockk<StorePicture>()

    private fun machine(): LocalStableDiffusionMachine =
        LocalStableDiffusionMachine(
            server.url("/").toString(),
            storePicture,
            steps = 8,
            width = 512,
            height = 512,
            timeout = 5.seconds,
        )

    @Test
    fun `stores the decoded image and returns Ready`() =
        runTest {
            // Given
            val pngBytes = byteArrayOf(1, 2, 3)
            val b64 = Base64.getEncoder().encodeToString(pngBytes)
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"images":["$b64"]}"""),
            )
            coEvery { storePicture handle StorePicture.Command(pngBytes, "image/png") } returns "xyz"

            // When
            val result = machine() answer Machine.Query(Prompt("dragon eating a cookie"))

            // Then
            assertEquals(Picture.Ready("/images/xyz"), result)
        }

    @Test
    fun `server error yields Failed`() =
        runTest {
            // Given
            server.enqueue(MockResponse().setResponseCode(500))

            // When / Then
            assertEquals(Picture.Failed, machine() answer Machine.Query(Prompt("anything")))
        }

    @Test
    fun `empty image list yields Failed`() =
        runTest {
            // Given
            server.enqueue(
                MockResponse().setHeader("Content-Type", "application/json").setBody("""{"images":[]}"""),
            )

            // When / Then
            assertEquals(Picture.Failed, machine() answer Machine.Query(Prompt("anything")))
        }

    companion object {
        private val server = MockWebServer()

        @JvmStatic
        @BeforeAll
        fun startServer() {
            server.start()
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server.shutdown()
        }
    }
}
