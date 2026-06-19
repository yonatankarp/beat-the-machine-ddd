package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.Machine
import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.application.usecase.ForfeitChallengeUseCase
import com.yonatankarp.beatthemachine.application.usecase.GetChallengeUseCase
import com.yonatankarp.beatthemachine.application.usecase.MakeGuessUseCase
import com.yonatankarp.beatthemachine.application.usecase.StartChallengeUseCase
import com.yonatankarp.beatthemachine.output.ai.PicturePregeneration
import com.yonatankarp.beatthemachine.output.ai.SeedMachine
import com.yonatankarp.beatthemachine.output.ai.SeedPromptSource
import kotlinx.coroutines.launch
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun promptSource(): PromptSource = SeedPromptSource()

    @Bean
    fun machine(): Machine = SeedMachine()

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
    fun pendingPictureRetryRunner(
        picturePregeneration: PicturePregeneration,
        pictureScope: PictureScope,
    ): ApplicationRunner =
        ApplicationRunner {
            // Launch the retry sweep into the picture scope; the boot thread must not block.
            pictureScope.launch { picturePregeneration.retryPending() }
        }

    @Bean
    fun startChallenge(
        promptSource: PromptSource,
        storeChallenge: StoreChallenge,
        picturePregeneration: PicturePregeneration,
    ): StartChallenge = StartChallengeUseCase(promptSource, storeChallenge, picturePregeneration::enqueue)

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
