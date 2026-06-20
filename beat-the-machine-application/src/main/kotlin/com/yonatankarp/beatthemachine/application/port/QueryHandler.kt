package com.yonatankarp.beatthemachine.application.port

fun interface QueryHandler<Q, R> {
    suspend infix fun answer(query: Q): R
}
