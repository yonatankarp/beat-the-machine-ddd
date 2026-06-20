package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.QueryHandler
import com.yonatankarp.beatthemachine.domain.valueobject.Picture
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

interface Machine : QueryHandler<Machine.Query, Picture> {
    data class Query(
        val prompt: Prompt,
    )
}
