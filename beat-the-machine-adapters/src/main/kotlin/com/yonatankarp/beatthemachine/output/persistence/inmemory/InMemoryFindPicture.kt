package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindPicture
import com.yonatankarp.beatthemachine.application.port.output.StoredImage

class InMemoryFindPicture(
    private val storage: InMemoryPictureStorage,
) : FindPicture {
    override suspend fun answer(query: FindPicture.Query): StoredImage? = storage.byId[query.id]
}
