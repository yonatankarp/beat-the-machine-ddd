package com.yonatankarp.beatthemachine.application.port.out

import com.yonatankarp.beatthemachine.domain.ChallengeId

class OptimisticLockConflict(
    val id: ChallengeId,
) : RuntimeException("challenge $id was modified concurrently; retry the operation")
