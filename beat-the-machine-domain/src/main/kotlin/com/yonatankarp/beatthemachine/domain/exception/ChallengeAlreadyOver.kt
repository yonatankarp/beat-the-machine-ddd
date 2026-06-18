package com.yonatankarp.beatthemachine.domain.exception

import com.yonatankarp.beatthemachine.domain.valueobject.ChallengeId

class ChallengeAlreadyOver(
    val id: ChallengeId,
) : RuntimeException("challenge $id is already over")
