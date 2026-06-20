package com.yonatankarp.beatthemachine.application.port.output

interface PictureStore {
    suspend fun save(
        bytes: ByteArray,
        contentType: String,
    ): String

    suspend fun load(id: String): StoredImage?
}

class StoredImage(
    val bytes: ByteArray,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoredImage) return false
        return contentType == other.contentType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + contentType.hashCode()
}
