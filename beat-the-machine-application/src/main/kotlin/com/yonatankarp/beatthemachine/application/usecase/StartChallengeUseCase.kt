package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Lives

class StartChallengeUseCase(
    private val promptSource: PromptSource,
    private val storeChallenge: StoreChallenge,
    private val enqueuePicture: (ChallengeId) -> Unit,
) : StartChallenge {
    override suspend fun handle(command: StartChallenge.Command): Challenge {
        val prompt = promptSource next command.difficulty
        val challenge = Challenge.start(prompt, Lives.forSecret(prompt, command.difficulty), difficulty = command.difficulty)
        val persisted = storeChallenge(challenge)
        enqueuePicture(persisted.id)
        return persisted
    }
}
