package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.exception.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.domain.entity.Challenge

class GetChallengeUseCase(
    private val findChallengeById: FindChallengeById,
) : GetChallenge {
    override suspend fun answer(query: GetChallenge.Query): Challenge = findChallengeById(query.id) ?: throw ChallengeNotFound(query.id)
}
