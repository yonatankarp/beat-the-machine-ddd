package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class FakeChallengeStore :
    StoreChallenge,
    FindChallengeById {
    val byId = linkedMapOf<ChallengeId, Challenge>()

    override suspend fun invoke(challenge: Challenge): Challenge = challenge.also { byId[it.id] = it }

    override suspend fun answer(query: FindChallengeById.Query): Challenge? = byId[query.id]
}
