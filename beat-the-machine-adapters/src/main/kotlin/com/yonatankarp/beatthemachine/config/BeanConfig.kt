package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.input.ForfeitChallenge
import com.yonatankarp.beatthemachine.application.port.input.GetChallenge
import com.yonatankarp.beatthemachine.application.port.input.MakeGuess
import com.yonatankarp.beatthemachine.application.port.input.StartChallenge
import com.yonatankarp.beatthemachine.application.port.out.ChallengeRepository
import com.yonatankarp.beatthemachine.application.port.out.Machine
import com.yonatankarp.beatthemachine.application.port.out.PromptSource
import com.yonatankarp.beatthemachine.application.service.ForfeitChallengeService
import com.yonatankarp.beatthemachine.application.service.GetChallengeService
import com.yonatankarp.beatthemachine.application.service.MakeGuessService
import com.yonatankarp.beatthemachine.application.service.StartChallengeService
import com.yonatankarp.beatthemachine.out.ai.SeedMachine
import com.yonatankarp.beatthemachine.out.ai.SeedPromptSource
import com.yonatankarp.beatthemachine.out.persistence.inmemory.InMemoryChallengeRepository
import com.yonatankarp.beatthemachine.out.persistence.sqlite.SqliteChallengeRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class BeanConfig {
    @Bean
    @ConditionalOnProperty(name = ["btm.persistence"], havingValue = "inmemory", matchIfMissing = true)
    fun inMemoryChallengeRepository(): ChallengeRepository = InMemoryChallengeRepository()

    @Bean
    @ConditionalOnProperty(name = ["btm.persistence"], havingValue = "sqlite")
    fun sqliteChallengeRepository(jdbcTemplate: JdbcTemplate): ChallengeRepository = SqliteChallengeRepository(jdbcTemplate)

    @Bean
    fun promptSource(): PromptSource = SeedPromptSource()

    @Bean
    fun machine(): Machine = SeedMachine()

    @Bean
    fun startChallenge(
        promptSource: PromptSource,
        repository: ChallengeRepository,
    ): StartChallenge =
        StartChallengeService(
            promptSource = promptSource,
            repository = repository,
            // TODO(Task 5.2): wire async PicturePregeneration.enqueue
            enqueuePicture = {},
        )

    @Bean
    fun makeGuess(repository: ChallengeRepository): MakeGuess = MakeGuessService(repository)

    @Bean
    fun getChallenge(repository: ChallengeRepository): GetChallenge = GetChallengeService(repository)

    @Bean
    fun forfeitChallenge(repository: ChallengeRepository): ForfeitChallenge = ForfeitChallengeService(repository)
}
