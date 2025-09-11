package com.yonatankarp.beatthemachine.application.ports

fun interface HelloWorldPort {
    suspend fun greet(): String
}
