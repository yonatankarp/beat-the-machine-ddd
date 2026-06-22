package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

val StorePictureCommandSuite by testSuite {
    given("a StorePicture command") {
        whenever("compared to one with the same bytes and content type") {
            then("they are equal") {
                val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                val same = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                command shouldBe same
                command.hashCode() shouldBe same.hashCode()
            }
        }

        whenever("compared to itself") {
            then("it is equal") {
                val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                (command.equals(command)).shouldBeTrue()
            }
        }

        whenever("compared to a value of another type") {
            then("it is not equal") {
                val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                (command.equals("image/png")).shouldBeFalse()
            }
        }

        whenever("compared to one with a different content type") {
            then("they are not equal") {
                val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                val other = StorePicture.Command(byteArrayOf(1, 2, 3), "image/jpeg")
                command shouldNotBe other
            }
        }

        whenever("compared to one with different bytes") {
            then("they are not equal") {
                val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
                val other = StorePicture.Command(byteArrayOf(9, 9, 9), "image/png")
                command shouldNotBe other
            }
        }
    }
}
