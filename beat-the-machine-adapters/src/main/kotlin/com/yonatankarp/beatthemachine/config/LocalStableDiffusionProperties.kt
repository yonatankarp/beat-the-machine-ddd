package com.yonatankarp.beatthemachine.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "btm.image.local-sd")
data class LocalStableDiffusionProperties(
    val baseUrl: String = "http://localhost:7860",
    val steps: Int = 8,
    val width: Int = 512,
    val height: Int = 512,
    val timeoutSeconds: Long = 120,
)
