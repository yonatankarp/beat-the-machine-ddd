package com.yonatankarp.beatthemachine.config

import com.yonatankarp.beatthemachine.application.port.output.PromptSource
import com.yonatankarp.beatthemachine.output.ai.ChatClientLlmText
import com.yonatankarp.beatthemachine.output.ai.SeedPromptSource
import com.yonatankarp.beatthemachine.output.ai.SpringAiPromptSource
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PromptSourceConfig {
    @Bean
    @ConditionalOnProperty(name = ["btm.prompt.provider"], havingValue = "seed", matchIfMissing = true)
    fun seedPromptSource(): PromptSource = SeedPromptSource()

    @Bean
    @ConditionalOnProperty(name = ["btm.prompt.provider"], havingValue = "ollama")
    fun ollamaPromptSource(chatClientBuilder: ChatClient.Builder): PromptSource = chatPromptSource(chatClientBuilder)

    @Bean
    @ConditionalOnProperty(name = ["btm.prompt.provider"], havingValue = "openai")
    fun openAiPromptSource(chatClientBuilder: ChatClient.Builder): PromptSource = chatPromptSource(chatClientBuilder)

    private fun chatPromptSource(chatClientBuilder: ChatClient.Builder): PromptSource =
        SpringAiPromptSource(ChatClientLlmText(chatClientBuilder.build()), SeedPromptSource())
}
