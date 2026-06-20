package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.QueryHandler

interface FindPicture : QueryHandler<FindPicture.Query, StoredImage?> {
    data class Query(
        val id: String,
    )
}
