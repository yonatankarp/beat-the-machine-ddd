package com.yonatankarp.beatthemachine.output.persistence.sqlite

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

val SqliteStorePictureITSuite by testSuite {
    test("handle returns an id and writes to picture table") {
        val adapters = newSqliteAdapters()
        val bytes = byteArrayOf(1, 2, 3, 4)
        val id = adapters.storePicture handle StorePicture.Command(bytes, "image/png")
        id.shouldNotBeBlank()
        val pictureRows = adapters.jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
        pictureRows shouldBe 1
    }

    test("bytes live in the picture table, not on challenge") {
        val adapters = newSqliteAdapters()
        adapters.storePicture handle StorePicture.Command(byteArrayOf(1), "image/png")
        val pictureRows = adapters.jdbc.queryForObject("SELECT COUNT(*) FROM picture", Int::class.java)
        val challengeRows = adapters.jdbc.queryForObject("SELECT COUNT(*) FROM challenge", Int::class.java)
        pictureRows shouldBe 1
        challengeRows shouldBe 0
    }
}
