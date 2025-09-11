package com.yonatankarp.beatthemachine.domain

data class Riddle(
    val id: Id,
    val prompt: List<Word>,
    val image: ImageUrl,
) {
    @JvmInline
    value class Id(
        val value: Int,
    )
}
