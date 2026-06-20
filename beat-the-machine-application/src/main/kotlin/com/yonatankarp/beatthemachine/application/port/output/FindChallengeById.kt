package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.QueryHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

interface FindChallengeById : QueryHandler<FindChallengeById.Query, Challenge?> {
    data class Query(
        val id: ChallengeId,
    )
}
