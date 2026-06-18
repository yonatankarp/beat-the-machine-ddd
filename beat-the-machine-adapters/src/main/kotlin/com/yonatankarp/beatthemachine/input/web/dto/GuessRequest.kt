package com.yonatankarp.beatthemachine.input.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GuessRequest(
    @field:NotBlank
    @field:Size(max = MAX_GUESS_LENGTH)
    val word: String,
) {
    companion object {
        const val MAX_GUESS_LENGTH = 100
    }
}
