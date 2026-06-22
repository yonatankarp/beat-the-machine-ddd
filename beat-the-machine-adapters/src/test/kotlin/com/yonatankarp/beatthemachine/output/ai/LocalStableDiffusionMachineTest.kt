package com.yonatankarp.beatthemachine.output.ai

import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt
import com.yonatankarp.testballoon.gwt.given
import com.yonatankarp.testballoon.gwt.then
import com.yonatankarp.testballoon.gwt.whenever
import de.infix.testBalloon.framework.core.TestConfig
import de.infix.testBalloon.framework.core.aroundAll
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.Base64
import kotlin.time.Duration.Companion.seconds

private val server = MockWebServer()

val LocalStableDiffusionMachineSuite by testSuite(
    testConfig =
        TestConfig.aroundAll { elementAction ->
            server.start()
            try {
                elementAction()
            } finally {
                server.shutdown()
            }
        },
) {
    val storePicture = mockk<StorePicture>()

    fun machine(): LocalStableDiffusionMachine =
        LocalStableDiffusionMachine(
            server.url("/").toString(),
            storePicture,
            steps = 8,
            width = 512,
            height = 512,
            timeout = 5.seconds,
        )

    given("a local Stable Diffusion machine") {
        whenever("the upstream returns a generated image") {
            then("it stores the decoded image and returns Ready") {
                val pngBytes = byteArrayOf(1, 2, 3)
                val b64 = Base64.getEncoder().encodeToString(pngBytes)
                server.enqueue(
                    MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody("""{"images":["$b64"]}"""),
                )
                coEvery { storePicture handle StorePicture.Command(pngBytes, "image/png") } returns "xyz"
                val result = machine() answer Machine.Query(Prompt("dragon eating a cookie"))
                result shouldBe Picture.Ready("/images/xyz")
            }
        }

        whenever("the upstream returns a server error") {
            then("it yields Failed") {
                server.enqueue(MockResponse().setResponseCode(500))
                machine() answer Machine.Query(Prompt("anything")) shouldBe Picture.Failed
            }
        }

        whenever("the upstream returns an empty image list") {
            then("it yields Failed") {
                server.enqueue(
                    MockResponse().setHeader("Content-Type", "application/json").setBody("""{"images":[]}"""),
                )
                machine() answer Machine.Query(Prompt("anything")) shouldBe Picture.Failed
            }
        }
    }
}
