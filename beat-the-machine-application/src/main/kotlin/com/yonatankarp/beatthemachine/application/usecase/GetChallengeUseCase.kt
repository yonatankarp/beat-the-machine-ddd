package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class GetChallengeUseCase(
    private val findChallengeById: FindChallengeById,
) : GetChallenge {
    override fun invoke(id: ChallengeId): Challenge = findChallengeById(id) ?: throw ChallengeNotFound(id)
}
