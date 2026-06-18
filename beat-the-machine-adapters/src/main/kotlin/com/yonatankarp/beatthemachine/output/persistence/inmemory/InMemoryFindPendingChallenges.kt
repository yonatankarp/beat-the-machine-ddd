package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

class InMemoryFindPendingChallenges(
    private val store: InMemoryChallengeStore,
) : FindPendingChallenges {
    override fun invoke(): List<Challenge> = store.byId.values.filter { it.picture is Picture.Pending }
}
