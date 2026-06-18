package com.yonatankarp.beatthemachine.domain

sealed interface Picture {
    data object Pending : Picture

    data class Ready(
        val url: String,
    ) : Picture

    data object Failed : Picture
}
