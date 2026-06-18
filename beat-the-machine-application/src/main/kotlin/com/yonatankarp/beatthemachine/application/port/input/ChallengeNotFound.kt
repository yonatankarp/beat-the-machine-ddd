package com.yonatankarp.beatthemachine.application.port.input

import com.yonatankarp.beatthemachine.domain.ChallengeId

class ChallengeNotFound(
    val id: ChallengeId,
) : RuntimeException("challenge $id not found")
