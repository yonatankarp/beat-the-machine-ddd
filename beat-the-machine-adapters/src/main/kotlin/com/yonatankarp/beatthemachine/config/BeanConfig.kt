package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.ChallengeTemplates
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.application.usecase.ForfeitChallengeUseCase
import com.yonatankarp.beatthemachine.application.usecase.GetChallengeUseCase
import com.yonatankarp.beatthemachine.application.usecase.MakeGuessUseCase
import com.yonatankarp.beatthemachine.application.usecase.StartChallengeUseCase
import com.yonatankarp.beatthemachine.output.ai.ChallengePoolReplenisher
import com.yonatankarp.beatthemachine.output.ai.ChallengeTemplateSeeder
import com.yonatankarp.beatthemachine.output.ai.PicturePregeneration
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class BeanConfig {
    @Bean
    fun pictureScope(): PictureScope = PictureScope()

    @Bean
    fun picturePregeneration(
        machine: Machine,
        findChallengeById: FindChallengeById,
        storeChallenge: StoreChallenge,
        findPendingChallenges: FindPendingChallenges,
        pictureScope: PictureScope,
    ): PicturePregeneration = PicturePregeneration(machine, findChallengeById, storeChallenge, findPendingChallenges, pictureScope)

    @Bean
    fun challengePoolReplenisher(
        promptSource: PromptSource,
        machine: Machine,
        challengeTemplates: ChallengeTemplates,
        pictureScope: PictureScope,
        @Value("\${btm.pool.target:10}") target: Int,
    ): ChallengePoolReplenisher = ChallengePoolReplenisher(promptSource, machine, challengeTemplates, pictureScope, target)

    @Bean
    fun challengeTemplateSeeder(challengeTemplates: ChallengeTemplates): ChallengeTemplateSeeder =
        ChallengeTemplateSeeder(challengeTemplates)

    @Bean
    fun poolWarmUpRunner(
        seeder: ChallengeTemplateSeeder,
        replenisher: ChallengePoolReplenisher,
        pictureScope: PictureScope,
    ): ApplicationRunner =
        ApplicationRunner {
            pictureScope.launch {
                seeder.seed()
                replenisher.warmUp()
            }
        }

    @Bean
    fun pendingPictureRetryRunner(
        picturePregeneration: PicturePregeneration,
        pictureScope: PictureScope,
    ): ApplicationRunner =
        ApplicationRunner {
            pictureScope.launch { picturePregeneration.retryPending() }
        }

    @Bean
    fun startChallenge(
        challengeTemplates: ChallengeTemplates,
        promptSource: PromptSource,
        storeChallenge: StoreChallenge,
        picturePregeneration: PicturePregeneration,
        challengePoolReplenisher: ChallengePoolReplenisher,
    ): StartChallenge =
        StartChallengeUseCase(
            challengeTemplates,
            promptSource,
            storeChallenge,
            picturePregeneration::enqueue,
            challengePoolReplenisher::replenish,
        )

    @Bean
    fun makeGuess(
        findChallengeById: FindChallengeById,
        storeChallenge: StoreChallenge,
    ): MakeGuess = MakeGuessUseCase(findChallengeById, storeChallenge)

    @Bean
    fun getChallenge(findChallengeById: FindChallengeById): GetChallenge = GetChallengeUseCase(findChallengeById)

    @Bean
    fun forfeitChallenge(
        findChallengeById: FindChallengeById,
        storeChallenge: StoreChallenge,
    ): ForfeitChallenge = ForfeitChallengeUseCase(findChallengeById, storeChallenge)
}
