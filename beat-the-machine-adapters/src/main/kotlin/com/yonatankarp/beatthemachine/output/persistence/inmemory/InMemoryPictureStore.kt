package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.PictureStore
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryPictureStore : PictureStore {
    private val byId = ConcurrentHashMap<String, StoredImage>()

    override suspend fun save(
        bytes: ByteArray,
        contentType: String,
    ): String {
        val id = UUID.randomUUID().toString()
        byId[id] = StoredImage(bytes.copyOf(), contentType)
        return "/images/$id"
    }

    override suspend fun load(id: String): StoredImage? = byId[id]
}
