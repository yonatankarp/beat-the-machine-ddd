package com.yonatankarp.beatthemachine.output.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.ai.chat.client.ChatClient

class ChatClientLlmText(
    private val chatClient: ChatClient,
) : LlmText {
    override suspend fun complete(
        system: String,
        user: String,
    ): String =
        withContext(Dispatchers.IO) {
            chatClient
                .prompt()
                .system(system)
                .user(user)
                .call()
                .content()
                .orEmpty()
        }
}
