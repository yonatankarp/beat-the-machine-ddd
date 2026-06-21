package com.yonatankarp.beatthemachine.application.port.output

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

val StorePictureCommandSuite by testSuite {
    test("commands with the same bytes and content type are equal") {
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val same = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        command shouldBe same
        command.hashCode() shouldBe same.hashCode()
    }

    test("a command equals itself") {
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        (command.equals(command)).shouldBeTrue()
    }

    test("a command does not equal a value of another type") {
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        (command.equals("image/png")).shouldBeFalse()
    }

    test("commands differing in content type are not equal") {
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val other = StorePicture.Command(byteArrayOf(1, 2, 3), "image/jpeg")
        command shouldNotBe other
    }

    test("commands differing in bytes are not equal") {
        val command = StorePicture.Command(byteArrayOf(1, 2, 3), "image/png")
        val other = StorePicture.Command(byteArrayOf(9, 9, 9), "image/png")
        command shouldNotBe other
    }
}
