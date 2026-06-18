package com.yonatankarp.beatthemachine.utils

fun String.toHiddenString(): String {
    val builder = StringBuilder()
    for (c in this) {
        builder.append(if (c != ' ') "-" else " ")
    }
    return builder.toString()
}
