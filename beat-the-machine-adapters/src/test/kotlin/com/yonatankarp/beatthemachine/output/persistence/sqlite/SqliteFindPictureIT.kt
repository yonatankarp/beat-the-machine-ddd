package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.test.dsl.given
import com.yonatankarp.beatthemachine.test.dsl.then
import com.yonatankarp.beatthemachine.test.dsl.whenever
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val SqliteFindPictureITSuite by testSuite {
    given("a stored picture") {
        whenever("loading it by id") {
            then("it round-trips the bytes and content type") {
                val (_, _, _, storePicture, findPicture) = newSqliteAdapters()
                val bytes = byteArrayOf(1, 2, 3, 4)
                val id = storePicture handle StorePicture.Command(bytes, "image/png")
                val loaded = (findPicture answer FindPicture.Query(id))!!
                loaded.contentType shouldBe "image/png"
                bytes.contentEquals(loaded.bytes) shouldBe true
            }
        }
    }

    given("an unknown id") {
        whenever("loading the picture") {
            then("it returns null") {
                val (_, _, _, _, findPicture) = newSqliteAdapters()
                (findPicture answer FindPicture.Query("missing")).shouldBeNull()
            }
        }
    }
}
