package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val InMemoryFindPictureSuite by testSuite {
    given("a stored picture") {
        whenever("finding it by id") {
            then("it round-trips bytes and content type") {
                val storage = InMemoryPictureStorage()
                val storePicture = InMemoryStorePicture(storage)
                val findPicture = InMemoryFindPicture(storage)
                val bytes = byteArrayOf(9, 8, 7)
                val id = storePicture handle StorePicture.Command(bytes, "image/png")
                val loaded = (findPicture answer FindPicture.Query(id))!!
                loaded.contentType shouldBe "image/png"
                bytes.contentEquals(loaded.bytes).shouldBeTrue()
            }
        }
    }

    given("an unknown id") {
        whenever("finding the picture") {
            then("it returns null") {
                val storage = InMemoryPictureStorage()
                val findPicture = InMemoryFindPicture(storage)
                (findPicture answer FindPicture.Query("nope")).shouldBeNull()
            }
        }
    }
}
