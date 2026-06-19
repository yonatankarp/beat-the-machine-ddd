package com.yonatankarp.beatthemachine.application.port.output

import com.yonatankarp.beatthemachine.application.exception.OptimisticLockConflict
import com.yonatankarp.beatthemachine.domain.entity.Challenge

/**
 * Persists a challenge, returning the stored aggregate with its version bumped.
 *
 * Implementations MUST perform an optimistic-locking check: compare the in-hand
 * [Challenge.version] against the stored version and throw [OptimisticLockConflict]
 * on a mismatch. The adapter is the single writer that increments the version;
 * domain operations leave it untouched.
 */
fun interface StoreChallenge {
    suspend operator fun invoke(challenge: Challenge): Challenge
}
