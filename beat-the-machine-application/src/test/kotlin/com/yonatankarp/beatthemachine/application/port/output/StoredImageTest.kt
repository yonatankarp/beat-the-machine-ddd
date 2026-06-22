package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

val StoredImageSuite by testSuite {
    given("a StoredImage") {
        whenever("compared to one with the same bytes and content type") {
            then("they are equal") {
                val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                val same = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                image shouldBe same
                image.hashCode() shouldBe same.hashCode()
            }
        }

        whenever("compared to itself") {
            then("it is equal") {
                val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                (image.equals(image)).shouldBeTrue()
            }
        }

        whenever("compared to a value of another type") {
            then("it is not equal") {
                val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                (image.equals("image/png")).shouldBeFalse()
            }
        }

        whenever("compared to one with a different content type") {
            then("they are not equal") {
                val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                val other = StoredImage(byteArrayOf(1, 2, 3), "image/jpeg")
                image shouldNotBe other
            }
        }

        whenever("compared to one with different bytes") {
            then("they are not equal") {
                val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
                val other = StoredImage(byteArrayOf(9, 9, 9), "image/png")
                image shouldNotBe other
            }
        }
    }
}
