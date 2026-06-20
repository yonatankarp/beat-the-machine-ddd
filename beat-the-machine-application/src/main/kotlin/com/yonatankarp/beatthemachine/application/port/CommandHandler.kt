package com.yonatankarp.beatthemachine.application.port

fun interface CommandHandler<C, R> {
    suspend infix fun handle(command: C): R
}
