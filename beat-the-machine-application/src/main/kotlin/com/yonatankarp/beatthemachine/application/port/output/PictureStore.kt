package com.yonatankarp.beatthemachine.application.port.output

/**
 * Persists rendered image bytes and serves them back by id. Image-addressed: the
 * store mints an id on save and returns a relative URL, so the rendering Machine
 * never needs to know about challenges. Keeps bytes out of the domain Picture.
 */
interface PictureStore {
    /** Stores [bytes] and returns a relative URL of the form "/images/{id}". */
    suspend fun save(
        bytes: ByteArray,
        contentType: String,
    ): String

    /** Loads a stored image by its bare id (the "{id}" segment), or null if absent. */
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
