package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

/**
 * In-memory test double backing the three segregated storage ports with a single
 * shared map (mirroring why the real persistence adapter is one class).
 */
class FakeChallengeStore :
    StoreChallenge,
    FindChallengeById,
    FindPendingChallenges {
    val byId = linkedMapOf<ChallengeId, Challenge>()

    override fun invoke(challenge: Challenge): Challenge = challenge.also { byId[it.id] = it }

    override fun invoke(id: ChallengeId): Challenge? = byId[id]

    override fun invoke(): List<Challenge> = byId.values.filter { it.picture is Picture.Pending }
}
