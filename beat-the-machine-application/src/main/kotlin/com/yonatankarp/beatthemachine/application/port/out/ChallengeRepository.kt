package com.yonatankarp.beatthemachine.application.port.out

import com.yonatankarp.beatthemachine.domain.Challenge
import com.yonatankarp.beatthemachine.domain.ChallengeId

interface ChallengeRepository {
    /**
     * Persists the given [challenge].
     *
     * Implementations MUST perform an optimistic-locking check: compare
     * [Challenge.version] against the stored version before writing. If the
     * versions do not match (the record was modified concurrently), throw
     * [OptimisticLockConflict]. The version increment itself is handled by the
     * domain (see [Challenge.withPicture]) or by the adapter on write.
     *
     * @return the saved [Challenge] (may carry an updated version after
     *         persistence, e.g. a DB-assigned sequence number).
     * @throws OptimisticLockConflict if the stored version differs from
     *         [challenge]'s version.
     */
    fun save(challenge: Challenge): Challenge

    /** Returns the [Challenge] with the given [id], or `null` if not found. */
    fun findById(id: ChallengeId): Challenge?
}
