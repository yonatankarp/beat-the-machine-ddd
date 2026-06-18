package com.yonatankarp.beatthemachine.application.service

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.port.out.PromptSource
import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId
import com.yonatankarp.beatthemachine.domain.Difficulty
import com.yonatankarp.beatthemachine.domain.Lives

class StartChallengeService(
    private val promptSource: PromptSource,
    private val repository: ChallengeRepository,
    private val enqueuePicture: (ChallengeId) -> Unit,
) : StartChallenge {
    private val startingLives = Lives(6)

    override fun start(difficulty: Difficulty): Challenge {
        val challenge = Challenge.start(promptSource.next(difficulty), startingLives)
        val persisted = repository.save(challenge)
        enqueuePicture(persisted.id)
        return persisted
    }
}
