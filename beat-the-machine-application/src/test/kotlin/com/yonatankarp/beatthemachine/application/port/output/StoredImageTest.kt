package com.yonatankarp.beatthemachine.application.port.output

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

val StoredImageSuite by testSuite {
    test("images with the same bytes and content type are equal") {
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val same = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        image shouldBe same
        image.hashCode() shouldBe same.hashCode()
    }

    test("an image equals itself") {
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        (image.equals(image)).shouldBeTrue()
    }

    test("an image does not equal a value of another type") {
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        (image.equals("image/png")).shouldBeFalse()
    }

    test("images differing in content type are not equal") {
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val other = StoredImage(byteArrayOf(1, 2, 3), "image/jpeg")
        image shouldNotBe other
    }

    test("images differing in bytes are not equal") {
        val image = StoredImage(byteArrayOf(1, 2, 3), "image/png")
        val other = StoredImage(byteArrayOf(9, 9, 9), "image/png")
        image shouldNotBe other
    }
}
