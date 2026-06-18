package com.yonatankarp.beatthemachine.input.web.dto

data class MaskedTokenDto(
    val revealed: Boolean,
    val word: String?,
)
