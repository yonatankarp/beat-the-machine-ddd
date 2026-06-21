package com.yonatankarp.beatthemachine.domain.valueobject

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

val PictureSuite by testSuite {
    test("Pending is a Picture") {
        // When
        val picture = Picture.Pending

        // Then
        picture.shouldBeInstanceOf<Picture.Pending>()
    }

    test("Ready carries url") {
        // Given
        val url = "https://example.com/img.png"

        // When
        val pic = Picture.Ready(url)

        // Then
        pic.shouldBeInstanceOf<Picture.Ready>()
        pic.url shouldBe "https://example.com/img.png"
    }

    test("Failed is a Picture") {
        // When
        val picture = Picture.Failed

        // Then
        picture.shouldBeInstanceOf<Picture.Failed>()
    }
}
