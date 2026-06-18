package com.yonatankarp.beatthemachine.domain

class ChallengeAlreadyOver(
    val id: ChallengeId,
) : RuntimeException("challenge $id is already over")
