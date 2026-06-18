package com.yonatankarp.beatthemachine.models

data class GuessRequest(
    var words: String? = null,
)

data class GuessResponse(
    var words: List<String>? = null,
) {
    enum class GuessResult(
        val value: String,
    ) {
        HIT("hit"),
        MISS("miss"),
    }
}
