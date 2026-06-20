package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.port.QueryHandler
import com.yonatankarp.beatthemachine.domain.entity.Challenge

interface FindPendingChallenges : QueryHandler<FindPendingChallenges.Query, List<Challenge>> {
    data object Query
}
