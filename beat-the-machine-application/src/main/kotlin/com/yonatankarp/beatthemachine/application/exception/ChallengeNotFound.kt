package com.yonatankarp.beatthemachine.application.exception

import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class ChallengeNotFound(
    val id: ChallengeId,
) : RuntimeException("challenge $id not found")
