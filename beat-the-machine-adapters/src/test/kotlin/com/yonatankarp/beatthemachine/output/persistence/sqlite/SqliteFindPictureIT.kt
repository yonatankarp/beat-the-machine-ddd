package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val SqliteFindPictureITSuite by testSuite {
    test("round-trips the bytes and content type") {
        val (_, _, _, storePicture, findPicture) = newSqliteAdapters()
        val bytes = byteArrayOf(1, 2, 3, 4)
        val id = storePicture handle StorePicture.Command(bytes, "image/png")
        val loaded = (findPicture answer FindPicture.Query(id))!!
        loaded.contentType shouldBe "image/png"
        bytes.contentEquals(loaded.bytes) shouldBe true
    }

    test("answer returns null for unknown id") {
        val (_, _, _, _, findPicture) = newSqliteAdapters()
        (findPicture answer FindPicture.Query("missing")).shouldBeNull()
    }
}
