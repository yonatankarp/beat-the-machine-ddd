package com.yonatankarp.beatthemachine.domain.exception

class InvalidGuess(
    reason: String,
) : RuntimeException(reason)
