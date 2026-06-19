package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class InMemoryFindChallengeById(
    private val store: InMemoryChallengeStore,
) : FindChallengeById {
    override suspend fun invoke(id: ChallengeId): Challenge? = store.byId[id]
}
