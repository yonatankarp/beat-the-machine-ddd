package com.yonatankarp.beatthemachine.application.usecase

import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import com.yonatankarp.beatthemachine.domain.valueobject.Difficulty
import com.yonatankarp.beatthemachine.domain.valueobject.Lives
import com.yonatankarp.beatthemachine.domain.valueobject.Picture

class StartChallengeUseCase(
    private val challengeTemplates: ChallengeTemplates,
    private val promptSource: PromptSource,
    private val storeChallenge: StoreChallenge,
    private val enqueuePicture: (ChallengeId) -> Unit,
    private val replenish: (Difficulty) -> Unit,
) : StartChallenge {
    override suspend fun handle(command: StartChallenge.Command): Challenge {
        val difficulty = command.difficulty
        val template = challengeTemplates.randomReady(difficulty)
        val challenge =
            if (template != null) {
                Challenge.start(
                    template.prompt,
                    Lives.forSecret(template.prompt, difficulty),
                    Picture.Ready(template.pictureUrl),
                    difficulty,
                )
            } else {
                val prompt = promptSource answer PromptSource.Query(difficulty)
                Challenge.start(prompt, Lives.forSecret(prompt, difficulty), difficulty = difficulty)
            }
        val persisted = storeChallenge handle StoreChallenge.Command(challenge)
        if (template == null) enqueuePicture(persisted.id)
        replenish(difficulty)
        return persisted
    }
}
