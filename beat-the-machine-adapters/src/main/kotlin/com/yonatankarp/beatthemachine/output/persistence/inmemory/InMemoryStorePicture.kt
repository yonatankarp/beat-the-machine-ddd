package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.StorePicture
import com.yonatankarp.beatthemachine.application.port.output.StoredImage
import java.util.UUID

class InMemoryStorePicture(
    private val storage: InMemoryPictureStorage,
) : StorePicture {
    override suspend fun handle(command: StorePicture.Command): String {
        val id = UUID.randomUUID().toString()
        storage.byId[id] = StoredImage(command.bytes.copyOf(), command.contentType)
        return id
    }
}
