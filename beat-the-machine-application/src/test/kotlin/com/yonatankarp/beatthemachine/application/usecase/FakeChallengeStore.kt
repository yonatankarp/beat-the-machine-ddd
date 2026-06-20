package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

class FakeChallengeStore :
    StoreChallenge,
    FindChallengeById,
    FindPendingChallenges {
    val byId = linkedMapOf<ChallengeId, Challenge>()

    override suspend fun invoke(challenge: Challenge): Challenge = challenge.also { byId[it.id] = it }

    override suspend fun invoke(id: ChallengeId): Challenge? = byId[id]

    override suspend fun invoke(): List<Challenge> = byId.values.filter { it.picture is Picture.Pending }
}
