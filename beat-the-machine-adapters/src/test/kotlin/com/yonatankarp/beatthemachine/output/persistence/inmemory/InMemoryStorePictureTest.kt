package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

val InMemoryStorePictureSuite by testSuite {
    test("handle returns an id and stores the bytes") {
        val storage = InMemoryPictureStorage()
        val storePicture = InMemoryStorePicture(storage)
        val bytes = byteArrayOf(9, 8, 7)
        val id = storePicture handle StorePicture.Command(bytes, "image/png")
        id.shouldNotBeBlank()
        val stored = storage.byId[id]!!
        stored.contentType shouldBe "image/png"
        bytes.contentEquals(stored.bytes).shouldBeTrue()
    }
}
