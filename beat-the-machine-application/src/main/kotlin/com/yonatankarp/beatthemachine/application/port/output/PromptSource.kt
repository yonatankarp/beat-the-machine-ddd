package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.QueryHandler
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

interface PromptSource : QueryHandler<PromptSource.Query, Prompt> {
    data class Query(
        val difficulty: Difficulty,
    )
}
