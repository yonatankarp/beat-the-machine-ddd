package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Prompt

class StartChallengeUseCase(
    private val promptSource: PromptSource,
    private val storeChallenge: StoreChallenge,
    private val enqueuePicture: (ChallengeId) -> Unit,
) : StartChallenge {
    override suspend fun invoke(difficulty: Difficulty): Challenge {
        val prompt = promptSource next difficulty
        val challenge = Challenge.start(prompt, Lives.forSecret(prompt, difficulty), difficulty = difficulty)
        val persisted = storeChallenge(challenge)
        enqueuePicture(persisted.id)
        return persisted
    }
}
