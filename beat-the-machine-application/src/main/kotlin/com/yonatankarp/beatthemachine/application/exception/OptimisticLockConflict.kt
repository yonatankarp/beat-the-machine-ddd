package com.yonatankarp.beatthemachine.application.exception

import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class OptimisticLockConflict(
    val id: ChallengeId,
) : RuntimeException("challenge $id was modified concurrently; retry the operation")
