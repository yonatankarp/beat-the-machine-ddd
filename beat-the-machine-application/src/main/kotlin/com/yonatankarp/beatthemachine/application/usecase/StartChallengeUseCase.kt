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

/**
 * Starts a challenge from the pre-seed pool when a ready template exists (instant
 * READY picture, no client polling), and degrades to on-demand generation (pending
 * picture filled asynchronously) when the pool for that difficulty is empty. Either
 * way it triggers a background top-up of the pool.
 */
class StartChallengeUseCase(
    private val challengeTemplates: ChallengeTemplates,
    private val promptSource: PromptSource,
    private val storeChallenge: StoreChallenge,
    private val enqueuePicture: (ChallengeId) -> Unit,
    private val replenish: (Difficulty) -> Unit,
) : StartChallenge {
    override suspend fun invoke(difficulty: Difficulty): Challenge {
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
                val prompt = promptSource next difficulty
                Challenge.start(prompt, Lives.forSecret(prompt, difficulty), difficulty = difficulty)
            }
        val persisted = storeChallenge(challenge)
        if (template == null) enqueuePicture(persisted.id)
        replenish(difficulty)
        return persisted
    }
}
