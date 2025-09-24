package com.yonatankarp.beatthemachine.domain.riddle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ImageUrlTest {
    @Test
    fun `should create image url with valid url`() {
        // Given
        val validUrl = "https://example.com/image.jpg"

        // When
        val imageUrl = ImageUrl(validUrl)

        // Then
        imageUrl.value shouldBe validUrl
    }

    @Test
    fun `should throw exception when creating image url with invalid url`() {
        // Given
        val invalidUrl = "not-a-valid-url"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageUrl(invalidUrl)
        }
    }
}
