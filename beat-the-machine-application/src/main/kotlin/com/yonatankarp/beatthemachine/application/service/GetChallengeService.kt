package com.yonatankarp.beatthemachine.application.service

import com.yonatankarp.beatthemachine.application.port.input.ChallengeNotFound
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId

class GetChallengeService(
    private val repository: ChallengeRepository,
) : GetChallenge {
    override fun get(id: ChallengeId): Challenge = repository.findById(id) ?: throw ChallengeNotFound(id)
}
