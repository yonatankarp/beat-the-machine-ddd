package com.yonatankarp.beatthemachine.domain.riddle

import java.net.URL

/**
 * Represents a validated URL pointing to an AI-generated image.
 *
 * @property value The URL string (validated during construction)
 * @throws IllegalArgumentException if the URL format is invalid
 */
data class ImageUrl(
    val value: String,
) {
    init {
        require(isValidUrl(value)) { "Invalid URL format" }
    }

    private fun isValidUrl(url: String) = runCatching { URL(url) }.isSuccess
}
