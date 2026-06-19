package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives

class StartChallengeUseCase(
    private val promptSource: PromptSource,
    private val storeChallenge: StoreChallenge,
    private val enqueuePicture: (ChallengeId) -> Unit,
) : StartChallenge {
    override suspend fun invoke(difficulty: Difficulty): Challenge {
        val challenge = Challenge.start(promptSource next difficulty, startingLives(difficulty))
        val persisted = storeChallenge(challenge)
        enqueuePicture(persisted.id)
        return persisted
    }

    private fun startingLives(difficulty: Difficulty): Lives =
        when (difficulty) {
            Difficulty.EASY -> Lives(8)
            Difficulty.MEDIUM -> Lives(6)
            Difficulty.HARD -> Lives(4)
        }
}
