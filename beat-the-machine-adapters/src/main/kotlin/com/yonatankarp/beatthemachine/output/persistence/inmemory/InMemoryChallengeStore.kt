package com.yonatankarp.beatthemachine.output.persistence.inmemory

import com.yonatankarp.beatthemachine.domain.entity.Challenge
import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Shared in-memory state for the challenge persistence operations. Its single
 * responsibility is holding challenges in memory; the operation adapters
 * (store / find / find-pending) own the behaviour and share this state, mirroring
 * how the SQLite operations share a [org.springframework.jdbc.core.JdbcTemplate].
 */
class InMemoryChallengeStore {
    val byId: ConcurrentMap<ChallengeId, Challenge> = ConcurrentHashMap()
}
