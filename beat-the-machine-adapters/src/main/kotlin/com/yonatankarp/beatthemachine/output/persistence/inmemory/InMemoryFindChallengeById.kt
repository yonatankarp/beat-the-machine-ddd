package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class InMemoryFindChallengeById(
    private val store: InMemoryChallengeStore,
) : FindChallengeById {
    override suspend fun answer(query: FindChallengeById.Query): Challenge? = store.byId[query.id]
}
