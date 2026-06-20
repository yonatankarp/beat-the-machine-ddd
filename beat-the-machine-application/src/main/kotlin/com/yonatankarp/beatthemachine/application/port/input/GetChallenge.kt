package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.application.port.QueryHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

interface GetChallenge : QueryHandler<GetChallenge.Query, Challenge> {
    data class Query(
        val id: ChallengeId,
    )
}
