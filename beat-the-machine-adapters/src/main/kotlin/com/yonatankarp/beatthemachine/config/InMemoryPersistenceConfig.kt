package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.FindChallengeById
import com.yonatankarp.beatthemachine.application.port.output.FindPendingChallenges
import com.yonatankarp.beatthemachine.application.port.output.StoreChallenge
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryChallengeStore
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindChallengeById
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryFindPendingChallenges
import com.yonatankarp.beatthemachine.output.persistence.inmemory.InMemoryStoreChallenge
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["btm.persistence"], havingValue = "inmemory", matchIfMissing = true)
class InMemoryPersistenceConfig {
    @Bean
    fun inMemoryChallengeStore(): InMemoryChallengeStore = InMemoryChallengeStore()

    @Bean
    fun storeChallenge(store: InMemoryChallengeStore): StoreChallenge = InMemoryStoreChallenge(store)

    @Bean
    fun findChallengeById(store: InMemoryChallengeStore): FindChallengeById = InMemoryFindChallengeById(store)

    @Bean
    fun findPendingChallenges(store: InMemoryChallengeStore): FindPendingChallenges = InMemoryFindPendingChallenges(store)
}
